package middleware.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import common.rm.ResourceManager;
import middleware.impl.exceptions.InvalidTransactionException;
import middleware.impl.exceptions.NotPreparedException;

public class TransactionManager {

	public static final long TRANSACTION_TIMEOUT = 60000;

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

	/**
	 * Initializes a new {@link TransactionManager}
	 */
	public TransactionManager(MiddlewareImpl middleware) {
		this.tid = 0;
		this.transactions = new HashMap<>();
		this.timers = new HashMap<>();
		this.middleware = middleware;

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
	public boolean prepareTransaction(int id) {
		// TODO
		setTransactionStatus(id, Status.PREPARED);
		return true;
	}

	/**
	 * Commits a transaction.
	 */
	public boolean commitTransaction(int id) throws RemoteException {
		log("Commiting transaction " + id);
		boolean success = true;

		if (transactions.containsKey(id)) {
			Transaction transaction = transactions.get(id);

			switch (transaction.status) {
			case ACTIVE:
				throw new NotPreparedException();

			case IN_PREPARATION:
			case PREPARED:
				setTransactionStatus(id, Status.IN_PREPARATION);
				for (String rm : transactions.get(id).rms) {
					success &= middleware.commit(rm, id);
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
	public boolean abortTransaction(int id) throws RemoteException {
		log("Aborting transaction " + id);
		boolean success = true;

		setTransactionStatus(id, Status.IN_ABORT);
		for (String rm : transactions.get(id).rms) {
			middleware.abort(rm, id);
		}

		Status status = success ? Status.ABORTED : Status.ACTIVE;
		setTransactionStatus(id, status);

		return success;
	}

	/**
	 * Adds a resource manager to the set of involved managers.
	 */
	public void enlist(int id, ResourceManager rm) {
		if (transactions.containsKey(id)) {
			String rmname = rm.getClass().getInterfaces()[0].getName();
			transactions.get(id).rms.add(rmname);
		}
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
					if (transactions.containsKey(id)) {
						log("Timeout. Aborting transaction " + id);
						setTransactionStatus(id, Status.TIMED_OUT);
						try {
							abortTransaction(id);
						} catch (RemoteException e) {
							// Ignore.
						}
					} else {
						timers.remove(id);
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
		log("Saving data to disk...");

		try {
			File file = new File("server-data/middleware/transactions.data");

			file.getParentFile().mkdirs();
			file.createNewFile();

			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);

			oos.writeObject(transactions);
			oos.close();

		} catch (IOException e) {
			log("Error: Unable to save to disk");
			e.printStackTrace();
			return false;
		}

		return true;

	}

	/**
	 * Restores the state of the transaction manager form the save file.
	 * {@link TransactionManager}, otherwise creates a new save file with the
	 * {@link TransactionManager#save()} method.
	 */
	@SuppressWarnings("unchecked")
	private void loadSave() {
		try {
			FileInputStream fis = new FileInputStream("server-data/middleware/transactions.data");
			ObjectInputStream ois = new ObjectInputStream(fis);
			HashMap<Integer, Transaction> data = (HashMap<Integer, Transaction>) ois.readObject();

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

			ois.close();
		} catch (IOException | ClassNotFoundException e) {
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
