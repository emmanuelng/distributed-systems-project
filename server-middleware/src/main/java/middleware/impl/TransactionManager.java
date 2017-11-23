package middleware.impl;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import common.rm.ResourceManager;
import middleware.impl.MiddlewareImpl.RM;
import middleware.impl.exceptions.InvalidTransactionException;
import middleware.impl.exceptions.NotPreparedException;

public class TransactionManager {

	public static final long TRANSACTION_TIMEOUT = 60000;

	/**
	 * Represents the status of a transaction
	 */
	public static enum Status {
		ACTIVE, PREPARED, COMMITTED, ABORTED, TIMED_OUT, INVALID
	}

	/**
	 * A private class representing a transaction
	 */
	private class Transaction {

		private Set<RM> rms; // The resource managers involved in the transaction
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
	 * 
	 * @param middleware
	 *            the middleware
	 */
	public TransactionManager(MiddlewareImpl middleware) {
		this.tid = 0;
		this.transactions = new HashMap<>();
		this.timers = new HashMap<>();
		this.middleware = middleware;
	}

	/**
	 * Initializes a transaction. Sets its status to {@link Status#ACTIVE} and
	 * initializes a timer.
	 * 
	 * @return the transaction id
	 */
	public int startTransaction() {
		tid++;

		System.out.println("[TransactionManager] Starting transaction " + tid);
		transactions.put(tid, new Transaction());
		resetTimeout(tid);

		return tid;
	}

	/**
	 * Prepares a transaction before being commited. Check that every resource
	 * managers can commit the transaction.
	 * 
	 * @param id
	 *            the transaction id
	 */
	public boolean prepareTransaction(int id) {
		// TODO
		transactions.get(id).status = Status.PREPARED;
		return true;
	}

	/**
	 * Commits a transaction.
	 * 
	 * @param id
	 *            the transaction id
	 */
	public boolean commitTransaction(int id) throws InvalidTransactionException, NotPreparedException {
		System.out.println("[TransactionManager] Commiting transaction " + id);
		boolean success = true;
		Transaction transaction = transactions.get(id);

		if (transaction != null && transaction.status == Status.PREPARED) {
			for (RM rmid : transactions.get(id).rms) {
				try {
					ResourceManager rm = middleware.rm(rmid);
					success &= rm.commit(id);
				} catch (RemoteException e) {
					System.err.println("[TransactionManager] An error occurred while commiting transaction " + id);
				}
			}
		} else if (transaction != null && transaction.status == Status.ACTIVE) {
			throw new NotPreparedException();
		} else {
			throw new InvalidTransactionException("Invalid transaction id");
		}

		transaction.status = success ? Status.COMMITTED : transaction.status;
		return success;
	}

	/**
	 * Aborts a transaction.
	 * 
	 * @param id
	 *            the transaction id.
	 */
	public boolean abortTransaction(int id) {
		System.out.println("[TransactionManager] Aborting transaction " + id);
		boolean success = true;

		for (RM rmid : transactions.get(id).rms) {
			try {
				ResourceManager rm = middleware.rm(rmid);
				success &= rm.abort(id);
			} catch (RemoteException e) {
				System.err.println("[TransactionManager] An error occurred while aborting transaction " + id);
			}
		}

		transactions.get(id).status = success ? Status.ABORTED : transactions.get(id).status;
		return success;
	}

	/**
	 * Adds a resource manager to the set of involved managers.
	 * 
	 * @param id
	 *            the transaction id
	 * @param rm
	 *            the resource manager id
	 */
	public void enlist(int id, RM rm) {
		if (transactions.containsKey(id)) {
			transactions.get(id).rms.add(rm);
		}
	}

	/**
	 * Returns the status of a transaction. If the transaction number is invalid,
	 * returns {@link Status#INVALID}
	 * 
	 * @param id
	 *            the transaction id
	 * @return the status
	 */
	public Status getStatus(int id) {
		return transactions.containsKey(id) ? transactions.get(id).status : Status.INVALID;
	}

	/**
	 * Resets the timer associated to the given transaction. Assumes that the given
	 * id number is valid.
	 * 
	 * @param id
	 *            the transaction id.
	 */
	public void resetTimeout(int id) {
		if (transactions.containsKey(id)) {
			// If the timer is reset, it means that the transaction must be active
			transactions.get(id).status = Status.ACTIVE;

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
						System.out.println("[TransactionManager] Timeout. Aborting transaction " + id);
						transactions.get(id).status = Status.TIMED_OUT;
						abortTransaction(id);
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
		return transactions.isEmpty();
	}

}
