package middleware.impl;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import common.rm.ResourceManager;

public class TransactionManager {

	public static final long TRANSACTION_TIMEOUT = 60000;

	private int id;
	private Map<Integer, Set<ResourceManager>> transactions;
	private Set<Integer> abortedTransactions;
	private Map<Integer, Timer> timers;

	public TransactionManager() {
		this.id = 0;
		this.transactions = new HashMap<>();
		this.abortedTransactions = new HashSet<>();
		this.timers = new HashMap<>();
	}

	public int startTransaction() {
		int transaction = id++;

		System.out.println("Starting transaction " + transaction);
		transactions.put(transaction, new HashSet<>());
		resetTimeout(transaction);

		return transaction;
	}

	public boolean commitTransaction(int id) {
		if (transactions.containsKey(id)) {
			System.out.println("Commiting transaction " + id);
			boolean success = true;

			for (ResourceManager rm : transactions.get(id)) {
				try {
					success &= rm.commit(id);
					System.out.println(rm.getClass().getSimpleName() + ": success=" + success);
				} catch (RemoteException e) {
					System.err.println("An error occurred while commiting transaction " + id);
				}
			}

			transactions.remove(id);
			return success;
		}
		return false;
	}

	public boolean abortTransaction(int id) {
		if (transactions.containsKey(id)) {
			System.out.println("Aborting transaction " + id);
			boolean success = true;

			for (ResourceManager rm : transactions.get(id)) {
				try {
					success &= rm.abort(id);
					System.out.println(rm.getClass().getSimpleName() + ": success=" + success);
				} catch (RemoteException e) {
					System.err.println("An error occurred while aborting transaction " + id);
				}
			}

			transactions.remove(id);
			abortedTransactions.add(id);
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

	public boolean isAborted(int id) {
		return abortedTransactions.contains(id);
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
					System.out.println("Transaction " + id + " waited for too long. Abort.");
					abortTransaction(id);
				}

			}, TRANSACTION_TIMEOUT);

			timers.put(id, timer);
		}
	}

}
