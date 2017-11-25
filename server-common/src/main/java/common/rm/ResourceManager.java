package common.rm;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ResourceManager extends Remote {

	/**
	 * Prepares a commit. Returns whether or no the RM is ready to commit.
	 * 
	 * @return <code>true</code> if it can commit, <code>false</code> if it wants to
	 *         abort.
	 */
	public boolean prepare(int id) throws RemoteException;

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
	
	/**
	 * Stops the server.
	 */
	public boolean selfDestroy(int status) throws RemoteException;
}
