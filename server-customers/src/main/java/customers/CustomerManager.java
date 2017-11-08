package customers;

import java.rmi.Remote;
import java.rmi.RemoteException;

import common.locks.DeadlockException;

public interface CustomerManager extends Remote {

	/**
	 * Returns a unique customer identifier.
	 * @throws DeadlockException 
	 */
	public int newCustomer(int id) throws RemoteException, DeadlockException;

	/**
	 * New customer with providing id.
	 * @throws DeadlockException 
	 */
	public boolean newCustomer(int id, int cid) throws RemoteException, DeadlockException;

	/**
	 * Removes the customer and associated reservations.
	 * 
	 * @return success
	 * @throws DeadlockException 
	 */
	public boolean deleteCustomer(int id, int cid) throws RemoteException, DeadlockException;

	/**
	 * Returns a bill.
	 * @throws DeadlockException 
	 */
	public String queryCustomerInfo(int id, int cid) throws RemoteException, DeadlockException;

	/**
	 * Adds a reservation to a customer.
	 * 
	 * @return success
	 * @throws DeadlockException 
	 */
	public boolean reserve(int id, int cid, String manager, String itemId, int price) throws RemoteException, DeadlockException;

	/**
	 * Cancels a reservation.
	 * 
	 * @return success
	 * @throws DeadlockException 
	 */
	public boolean cancelReservation(int id, int cid, String manager, String itemId) throws DeadlockException;

	/**
	 * Returns a string containing the customer's reservation. A reservation array
	 * has the following format: manager/itemId/amount
	 * 
	 * @return the string
	 * @throws DeadlockException 
	 */
	public String queryReservations(int id, int cid) throws RemoteException, DeadlockException;

	/**
	 * Removes all the reservations associated with the given item.
	 * @throws DeadlockException 
	 */
	public void clearReservationsForItem(int id, String itemId) throws RemoteException, DeadlockException;

	/**
	 * Starts a new transaction
	 * 
	 * @return success
	 */
	public boolean start(int id);

	/**
	 * Commits a transaction.
	 * 
	 * @return success
	 */
	public boolean commit(int id);

	/**
	 * Aborts a transaction.
	 * 
	 * @return success
	 */
	public boolean abort(int id);

}
