package middleware.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import common.transactions.TransactionHandler;

public class TransactionManager {

	private int id;
	private Map<Integer, Set<TransactionHandler>> transactions;

	public TransactionManager() {
		this.id = 0;
		this.transactions = new HashMap<>();
	}

	public int startTransaction() {
		int transaction = id++;
		transactions.put(transaction, new HashSet<>());
		return transaction;
	}

	public boolean commitTransaction(int id) {
		if (transactions.containsKey(id)) {
			boolean success = true;

			for (TransactionHandler handler : transactions.get(id)) {
				success &= handler.commit(id);
			}

			transactions.remove(id);
			return success;
		}
		return false;
	}

	public boolean abortTransaction(int id) {
		if (transactions.containsKey(id)) {
			boolean success = true;

			for (TransactionHandler handler : transactions.get(id)) {
				success &= handler.abort(id);
			}

			transactions.remove(id);
			return success;
		}

		return false;
	}

	public void enlist(int id, TransactionHandler handler) {
		if (transactions.containsKey(id)) {
			transactions.get(id).add(handler);
		}
	}
	
	public boolean isValid(int id) {
		return transactions.containsKey(id);
	}

}
