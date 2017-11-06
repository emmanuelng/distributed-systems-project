package customers;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CustomerManager extends Remote {

	/**
	 * Returns a unique customer identifier.
	 */
	public int newCustomer(int id) throws RemoteException;

	/**
	 * New customer with providing id.
	 */
	public boolean newCustomer(int id, int cid) throws RemoteException;

	/**
	 * Removes the customer and associated reservations.
	 * 
	 * @return success
	 */
	public boolean deleteCustomer(int id, int cid) throws RemoteException;

	/**
	 * Returns a bill.
	 */
	public String queryCustomerInfo(int id, int cid) throws RemoteException;

	/**
	 * Adds a reservation to a customer.
	 * 
	 * @return success
	 */
	public boolean reserve(int id, int cid, String manager, String itemId, int price) throws RemoteException;

	/**
	 * Cancels a reservation.
	 * 
	 * @return success
	 */
	public boolean cancelReservation(int id, int cid, String manager, String itemId);

	/**
	 * Returns a string containing the customer's reservation. A reservation array
	 * has the following format: manager/itemId/amount
	 * 
	 * @return the string
	 */
	public String queryReservations(int id, int cid) throws RemoteException;

	/**
	 * Removes all the reservations associated with the given item.
	 */
	public void clearReservationsForItem(int id, String itemId) throws RemoteException;

}
