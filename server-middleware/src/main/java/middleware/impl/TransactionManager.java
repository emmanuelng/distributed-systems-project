package middleware.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import common.files.SaveFile;
import common.rm.ResourceManager;
import middleware.impl.debug.CrashInjector;
import middleware.impl.exceptions.InvalidTransactionException;

public class TransactionManager {

	public static final long TRANSACTION_TIMEOUT = 60000;
	public static final long RESPONSE_TIMEOUT = 1000;

	/**
	 * Represents the status of a transaction
	 */
	public static enum Status {
		ACTIVE, IN_PREPARE, IN_COMMIT, COMMITTED, IN_ABORT, ABORTED, TIMED_OUT, INVALID
	}

	/**
	 * A private class representing a transaction.
	 */
	private static class Transaction implements Serializable {

		private static final long serialVersionUID = 2084803164578661556L;
		private Set<String> rms; // The resource managers involved in the transaction
		private Status status;

		Transaction() {
			this.rms = new HashSet<>();
			this.status = Status.ACTIVE;
		}
	}

	private int tid;
	private MiddlewareImpl middleware;
	private Map<Integer, Transaction> transactions;
	private Map<Integer, Timer> timers;

	private SaveFile<Map<Integer, Transaction>> saveFile;
	private CrashInjector crashInjector;

	/**
	 * Initializes a new {@link TransactionManager}
	 */
	public TransactionManager(MiddlewareImpl middleware) {
		this.tid = 0;
		this.transactions = new HashMap<>();
		this.timers = new HashMap<>();
		this.middleware = middleware;
		this.saveFile = new SaveFile<>("middleware", "transactions");
		this.crashInjector = new CrashInjector();

		loadSave();
	}

	/**
	 * Initializes a transaction. Sets its status to {@link Status#ACTIVE} and
	 * initializes a timer.
	 */
	public int startTransaction() {
		tid++;

		log("Starting transaction " + tid);
		transactions.put(tid, new Transaction());
		resetTimeout(tid);

		save();
		return tid;
	}

	/**
	 * Prepares a transaction before being committed. Check that every resource
	 * managers can commit the transaction.
	 */
	public boolean prepareTransaction(int id) throws InvalidTransactionException {
		Transaction transaction = transactions.get(id);

		if (transaction == null) {
			throw new InvalidTransactionException("The transaction does not exist.");
		}

		if (transaction.status != Status.ACTIVE && transaction.status != Status.IN_PREPARE) {
			if (transaction.status == Status.IN_COMMIT) {
				return commitTransaction(id);
			} else {
				throw new InvalidTransactionException(
						"Cannot prepare this transaction (status: " + transaction.status + ").");
			}
		}

		boolean success = true;
		ExecutorService executor = Executors.newSingleThreadExecutor();
		int i = 1;

		crashInjector.beforePrepare();
		log("Preparing transaction " + id);
		setTransactionStatus(id, Status.IN_PREPARE);

		try {
			for (String rm : transaction.rms) {
				Future<Boolean> future = executor.submit(new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						log("Sending prepare request to " + rm);
						middleware.prepare(rm, id);
						return middleware.prepare(rm, id);
					}
				});

				// One resource manager voted NO
				if (!future.get(RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS)) {
					log("Negative response received. Returning false...");
					success = false;
					break;
				}

				crashInjector.inPrepare(i, transactions.get(id).rms.size());
				i++;
			}
		} catch (TimeoutException | ExecutionException | InterruptedException e) {
			// One resource manager crashed
			success = false;
		}

		crashInjector.afterPrepare();

		if (success) {
			return commitTransaction(id);
		} else {
			setTransactionStatus(id, Status.ACTIVE);
		}

		return false;
	}

	/**
	 * Commits a transaction.
	 */
	private boolean commitTransaction(int id) throws InvalidTransactionException {
		Transaction transaction = transactions.get(id);

		if (transaction == null) {
			throw new InvalidTransactionException("The transaction does not exist.");
		}

		if (transaction.status != Status.IN_PREPARE && transaction.status != Status.IN_COMMIT) {
			throw new InvalidTransactionException(
					"Cannot commit this transaction (status: " + transaction.status + ").");
		}

		boolean success = true;
		ExecutorService executor = Executors.newSingleThreadExecutor();
		int i = 1;

		crashInjector.beforeDecision();
		log("Commiting transaction " + id);
		setTransactionStatus(id, Status.IN_COMMIT);

		for (String rm : transactions.get(id).rms) {
			try {
				Future<Boolean> future = executor.submit(new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						log("Sending commit request to " + rm);
						return middleware.commit(rm, id);
					}
				});

				success &= future.get(RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
				crashInjector.inDecision(i, transactions.get(id).rms.size());
				i++;
			} catch (TimeoutException | ExecutionException | InterruptedException e) {
				// One resource manager crashed
				success = false;
				break;
			}
		}

		Status status = success ? Status.COMMITTED : Status.IN_COMMIT;
		setTransactionStatus(id, status);
		crashInjector.afterDecision();

		return success;
	}

	/**
	 * Aborts a transaction.
	 */
	public boolean abortTransaction(int id) throws InvalidTransactionException {
		Transaction transaction = transactions.get(id);

		if (transaction == null) {
			throw new InvalidTransactionException("The transaction does not exist");
		}

		if (transaction.status != Status.ACTIVE && transaction.status != Status.IN_ABORT) {
			throw new InvalidTransactionException(
					"Cannot abort this transaction (status: " + transaction.status + ").");
		}

		boolean success = true;
		ExecutorService executor = Executors.newSingleThreadExecutor();
		int i = 1;

		crashInjector.beforeDecision();
		log("Aborting transaction " + id);
		setTransactionStatus(id, Status.IN_ABORT);

		for (String rm : transactions.get(id).rms) {
			try {
				Future<Boolean> future = executor.submit(new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						log("Sending abort request to " + rm);
						return middleware.abort(rm, id);
					}
				});

				success &= future.get(RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
				crashInjector.inDecision(i, transactions.get(id).rms.size());
				i++;
			} catch (TimeoutException | ExecutionException | InterruptedException e) {
				// One resource manager crashed
				success = false;
			}
		}

		Status status = success ? Status.ABORTED : Status.IN_ABORT;
		setTransactionStatus(id, status);
		crashInjector.afterDecision();

		return success;
	}

	/**
	 * Adds a resource manager to the set of involved managers.
	 */
	public void enlist(int id, ResourceManager rm) {
		String rmname = rm.getClass().getInterfaces()[0].getName();
		log("Enlisting " + rmname + " for transaction " + id);
		transactions.get(id).rms.add(rmname);
		save();
	}

	/**
	 * Returns the status of a transaction. If the transaction number is invalid,
	 * returns {@link Status#INVALID}
	 */
	public Status getStatus(int id) {
		return transactions.containsKey(id) ? transactions.get(id).status : Status.INVALID;
	}

	/**
	 * Resets the timer associated to the given transaction. Assumes that the given
	 * id number is valid.
	 */
	public void resetTimeout(int id) {
		if (transactions.containsKey(id)) {
			// Cancel the previous timer
			if (timers.containsKey(id)) {
				timers.get(id).cancel();
			}

			// Create a new timer
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						if (transactions.containsKey(id)) {
							Status status = transactions.get(id).status;
							if (status == Status.ACTIVE) {
								log("Timeout. Aborting transaction " + id);
								abortTransaction(id);
								setTransactionStatus(id, Status.TIMED_OUT);
							}
						} else {
							timers.remove(id);
						}
					} catch (Exception e) {
						// Ignore.
					}
				}
			}, TRANSACTION_TIMEOUT);

			timers.put(id, timer);
		}
	}

	/**
	 * Checks if the system can be shutdown gracefully. It is the case when no
	 * transaction is active in the system.
	 */
	public boolean canShutDown() {
		for (Transaction transaction : transactions.values()) {
			if (transaction.status == Status.ACTIVE) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Injects a crash in the middleware.
	 * 
	 * @see MiddlewareImpl#injectCrash(String, String, String)
	 */
	boolean injectCrash(String when, String operation) {
		return crashInjector.inject(when, operation);
	}

	/**
	 * Changes the status of a transaction and saves it to disk.
	 */
	private void setTransactionStatus(int id, Status status) {
		Transaction transaction = transactions.get(id);
		if (transaction != null && transaction.status != status) {
			log("Setting status of transaction " + id + " to " + status);
			transaction.status = status;
			save();
		}
	}

	/**
	 * Saves the state of the {@link TransactionManager} to disk. If the save file
	 * does not exist, creates a new one.
	 */
	private boolean save() {
		try {
			saveFile.save(transactions);
		} catch (IOException e) {
			log("Error: Unable to save to disk");
			return false;
		}

		return true;

	}

	/**
	 * Restores the state of the transaction manager form the save file.
	 * {@link TransactionManager}, otherwise creates a new save file with the
	 * {@link TransactionManager#save()} method.
	 */
	private void loadSave() {
		try {
			Map<Integer, Transaction> data = saveFile.read();
			for (Entry<Integer, Transaction> entry : data.entrySet()) {
				tid = entry.getKey() > tid ? entry.getKey() : tid;
				transactions.put(entry.getKey(), entry.getValue());

				switch (entry.getValue().status) {
				case ACTIVE:
					resetTimeout(entry.getKey());
					break;

				case IN_PREPARE:
				case IN_COMMIT:
					prepareTransaction(entry.getKey());
					break;

				case IN_ABORT:
					abortTransaction(entry.getKey());
					break;

				default:
					break;
				}
			}

		} catch (Exception e) {
			save();
		}
	}

	/**
	 * Writes a message to standard out.
	 */
	private void log(String message) {
		System.out.println("[TransactionManager] " + message);
	}

}
