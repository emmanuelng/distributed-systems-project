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
	public String queryCustomerInfo(int id, int cid);

	/**
	 * Adds a reservation to a customer.
	 * 
	 * @return success
	 */
	public boolean reserve(int id, int cid, String itemId, int price);

	/**
	 * Returns an array containing all the customer's reservation in the form of
	 * strings formatted as "manager/itemId/amount".
	 * 
	 * @return the array or <code>null</code> if the customer does not exist
	 */
	public String[] queryReservations(int id, int cid);

}
