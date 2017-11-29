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
import middleware.impl.exceptions.InvalidTransactionException;
import middleware.impl.exceptions.NotPreparedException;

public class TransactionManager {

	public static final long TRANSACTION_TIMEOUT = 60000;
	public static final long RESPONSE_TIMEOUT = 1000;

	/**
	 * Represents the status of a transaction
	 */
	public static enum Status {
		ACTIVE, // The transaction is active (not saved)
		IN_PREPARATION, // The prepare phase was started, but no decision was taken
		PREPARED, // The first phase is done. The transaction is ready to commit.
		IN_COMMIT, // The commit was started, but not all servers received the request
		COMMITTED, // The transaction is committed on all sites
		IN_ABORT, // The abort was started, but not all servers received the request
		ABORTED, // The transaction is aborted on all sites
		TIMED_OUT, // The transaction was aborted due to a timeout
		INVALID // An error occurred during the transaction processing
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

	/**
	 * Initializes a new {@link TransactionManager}
	 */
	public TransactionManager(MiddlewareImpl middleware) {
		this.tid = 0;
		this.transactions = new HashMap<>();
		this.timers = new HashMap<>();
		this.middleware = middleware;
		this.saveFile = new SaveFile<>("middleware", "transactions");

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
		log("Preparing transaction " + id);
		setTransactionStatus(id, Status.IN_PREPARATION);

		boolean success = true;
		ExecutorService executor = Executors.newSingleThreadExecutor();

		try {
			for (String rm : transactions.get(id).rms) {
				Future<Boolean> future = executor.submit(new Callable<Boolean>() {

					@Override
					public Boolean call() throws Exception {
						log("Sending prepare request to " + rm);
						middleware.prepare(rm, id);
						return middleware.prepare(rm, id);
					}

				});

				if (!future.get(RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS)) {
					log("Negative response received. Aborting transaction...");
					abortTransaction(id);
					success = false;
				}
			}

			setTransactionStatus(id, Status.PREPARED);
		} catch (TimeoutException | ExecutionException | InterruptedException e) {
			log("Timeout. Aborting transaction...");
			abortTransaction(id);
			success = false;
		}

		return success;
	}

	/**
	 * Commits a transaction.
	 */
	public boolean commitTransaction(int id) throws NotPreparedException, InvalidTransactionException {
		boolean success = true;

		if (transactions.containsKey(id)) {
			Transaction transaction = transactions.get(id);

			switch (transaction.status) {
			case ACTIVE:
				throw new NotPreparedException();

			case IN_PREPARATION:
			case PREPARED:
				log("Commiting transaction " + id);

				ExecutorService executor = Executors.newSingleThreadExecutor();
				setTransactionStatus(id, Status.IN_PREPARATION);

				try {
					for (String rm : transactions.get(id).rms) {
						Future<Boolean> future = executor.submit(new Callable<Boolean>() {

							@Override
							public Boolean call() throws Exception {
								log("Sending commit request to " + rm);
								return middleware.commit(rm, id);
							}
						});

						success &= future.get(RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
					}

				} catch (TimeoutException | ExecutionException | InterruptedException e) {
					log("Timeout. Aborting transaction...");
					abortTransaction(id);
					success = false;
				}

				break;

			default:
				throw new InvalidTransactionException("Invalid transaction id.");
			}
		}

		Status status = success ? Status.COMMITTED : Status.ACTIVE;
		setTransactionStatus(id, status);
		return success;
	}

	/**
	 * Aborts a transaction.
	 */
	public boolean abortTransaction(int id) throws InvalidTransactionException {
		log("Aborting transaction " + id);
		boolean success = true;

		if (transactions.containsKey(id)) {
			setTransactionStatus(id, Status.IN_ABORT);

			ExecutorService executor = Executors.newSingleThreadExecutor();

			for (String rm : transactions.get(id).rms) {
				try {
					Future<Boolean> future = executor.submit(new Callable<Boolean>() {

						@Override
						public Boolean call() throws Exception {
							log("Sending abort request to " + rm);
							return middleware.abort(rm, id);
						}
					});

					future.get(RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);

				} catch (TimeoutException | ExecutionException | InterruptedException e) {
					log("Timeout. Going to the next resource manager...");
					success = false;
				}
			}

			Status status = success ? Status.ABORTED : Status.ACTIVE;
			setTransactionStatus(id, status);

		} else {
			throw new InvalidTransactionException("The transaction is not active.");
		}

		return success;
	}

	/**
	 * Adds a resource manager to the set of involved managers.
	 */
	public void enlist(int id, ResourceManager rm) {
		String rmname = rm.getClass().getInterfaces()[0].getName();
		log("Enlisting " + rmname + " for transaction " + id);
		transactions.get(id).rms.add(rmname);
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
			// If the timer is reset, it means that the transaction must be active
			setTransactionStatus(id, Status.ACTIVE);

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
							if (status != Status.COMMITTED && status != Status.ABORTED && status != Status.IN_ABORT
									&& status != Status.TIMED_OUT) {
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
	 * Changes the status of a transaction and saves it to disk.
	 */
	private void setTransactionStatus(int id, Status status) {
		Transaction transaction = transactions.get(id);
		if (transaction != null && transaction.status != status) {
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
				case IN_PREPARATION:
					prepareTransaction(entry.getKey());
					break;
				case IN_COMMIT:
					try {
						commitTransaction(entry.getKey());
					} catch (InvalidTransactionException | NotPreparedException e) {
						// Ignore. These exceptions should not occur.
					}
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
