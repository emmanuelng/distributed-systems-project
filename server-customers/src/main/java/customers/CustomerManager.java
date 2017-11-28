package customers;

import java.rmi.RemoteException;

import common.locks.DeadlockException;
import common.rm.ResourceManager;

public interface CustomerManager extends ResourceManager {

	/**
	 * Returns a unique customer identifier.
	 */
	public int newCustomer(int id) throws RemoteException, DeadlockException;

	/**
	 * New customer with providing id.
	 */
	public boolean newCustomer(int id, int cid) throws RemoteException, DeadlockException;

	/**
	 * Removes the customer and associated reservations.
	 * 
	 * @return success
	 */
	public boolean deleteCustomer(int id, int cid) throws RemoteException, DeadlockException;

	/**
	 * Returns a bill.
	 */
	public String queryCustomerInfo(int id, int cid) throws RemoteException, DeadlockException;

	/**
	 * Adds a reservation to a customer.
	 * 
	 * @return success
	 */
	public boolean reserve(int id, int cid, String manager, String itemId, int price)
			throws RemoteException, DeadlockException;

	/**
	 * Cancels a reservation.
	 * 
	 * @return success
	 */
	public boolean cancelReservation(int id, int cid, String manager, String itemId) throws DeadlockException;

	/**
	 * Returns a string containing the customer's reservation. A reservation array
	 * has the following format: manager/itemId/amount
	 * 
	 * @return the string
	 */
	public String queryReservations(int id, int cid) throws RemoteException, DeadlockException;

	/**
	 * Removes all the reservations associated with the given item.
	 */
	public void clearReservationsForItem(int id, String itemId) throws RemoteException, DeadlockException;
}
