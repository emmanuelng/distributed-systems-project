package common.transactions;

/**
 * A transaction handler is any object that deals with transactions.
 */
public interface TransactionHandler {

	/**
	 * Commits a transaction.
	 */
	public boolean commit(int id);

	/**
	 * Aborts a transaction.
	 */
	public boolean abort(int id);

}
