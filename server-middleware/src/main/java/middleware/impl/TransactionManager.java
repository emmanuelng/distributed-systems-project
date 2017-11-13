package middleware.impl;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;

import common.rm.ResourceManager;

public class TransactionManager {

	public static final long TRANSACTION_TIMEOUT = 60000;

	private int id;
	private Map<Integer, Set<ResourceManager>> transactions;
	private Set<Integer> timedOutTransactions;
	private Map<Integer, Timer> timers;
	//private ArrayList<Long> startTimes;

	public TransactionManager() {
		this.id = 0;
		this.transactions = new HashMap<>();
		this.timedOutTransactions = new HashSet<>();
		this.timers = new HashMap<>();
		//this.startTimes = new ArrayList<>();
	}

	public int startTransaction() {
        long startTransTime = System.nanoTime();
	    int transaction = id++;
		//startTimes.add(id,startTransTime);
		System.out.println("[TransactionManager] Starting transaction " + transaction + ": " + startTransTime);
		transactions.put(transaction, new HashSet<>());
		resetTimeout(transaction);

		return transaction;
	}

	public boolean commitTransaction(int id) {
	    long commitStart = System.nanoTime();
        System.out.println("[TransactionManager] Commiting start time " + id +": " + commitStart );
		if (transactions.containsKey(id)) {

			System.out.println("[TransactionManager] Commiting transaction " + id);
			boolean success = true;

			for (ResourceManager rm : transactions.get(id)) {
				try {
					success &= rm.commit(id);
				} catch (RemoteException e) {
					System.err.println("[TransactionManager] An error occurred while commiting transaction " + id);
				}
			}
			transactions.remove(id);
            //long commitTransTime = System.nanoTime() - startTimes.get(id);
            long commitTransTime = System.nanoTime();
            System.out.println("[TransactionManager] Transaction " + id + " commited at: " + commitTransTime);
			return success;
		}
		return false;
	}

	public boolean abortTransaction(int id) {
        long abortStart = System.nanoTime();
        System.out.println("[TransactionManager] Aborting start time " + id +": " + abortStart );
		if (transactions.containsKey(id)) {
			System.out.println("[TransactionManager] Aborting transaction " + id);
			boolean success = true;

			for (ResourceManager rm : transactions.get(id)) {
				try {
					success &= rm.abort(id);
				} catch (RemoteException e) {
					System.err.println("[TransactionManager] An error occurred while aborting transaction " + id);
				}
			}

			transactions.remove(id);
           // long abortTransTime = System.nanoTime() - startTimes.get(id);
            long abortTransTime = System.nanoTime();
            System.out.println("[TransactionManager] Transaction " + id + " aborted at: " + abortTransTime);
			return success;
		}

		return false;
	}

	public void enlist(int id, ResourceManager rm) {
		if (transactions.containsKey(id)) {
			transactions.get(id).add(rm);
		}
	}

	public boolean isValid(int id) {
		return transactions.containsKey(id);
	}

	public boolean isTimedOut(int id) {
		return timedOutTransactions.contains(id);
	}

	public void resetTimeout(int id) {
		if (transactions.containsKey(id)) {

			if (timers.containsKey(id)) {
				timers.get(id).cancel();
			}

			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					if (transactions.containsKey(id)) {
						System.out.println("[TransactionManager] Timeout. Aborting transaction " + id);
						timedOutTransactions.add(id);
						abortTransaction(id);
                        //long abortTransTime = System.nanoTime() - startTimes.get(id);
                        long abortTransTime = System.nanoTime();
                        System.out.println("[TransactionManager] Transaction " + id + " aborted at: " + abortTransTime);
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
