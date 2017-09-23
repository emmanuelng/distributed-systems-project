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
	 */
	public boolean deleteCustomer(int id, int customer) throws RemoteException;

	/**
	 * Returns a bill
	 */
	public String queryCustomerInfo(int id, int customer) throws RemoteException;

}
