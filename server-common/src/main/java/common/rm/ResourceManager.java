package common.rm;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ResourceManager extends Remote {

	/**
	 * Commits a transaction.
	 */
	public boolean commit(int id) throws RemoteException;

	/**
	 * Aborts a transaction.
	 */
	public boolean abort(int id) throws RemoteException;
	
	/**
	 * Shuts down the resource manager
	 */
	public boolean shutdown() throws RemoteException;
}
