package common.transactions;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * A transaction handler is any object that deals with transactions.
 */
public interface TransactionHandler extends Remote {

	/**
	 * Commits a transaction.
	 */
	public boolean commit(int id) throws RemoteException;

	/**
	 * Aborts a transaction.
	 */
	public boolean abort(int id) throws RemoteException;

}
