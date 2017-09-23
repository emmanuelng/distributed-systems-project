package customers;

import java.rmi.Remote;

public interface CustomerManager extends Remote {

	/**
	 * Returns a unique customer identifier.
	 */
	public int newCustomer(int id);

	/**
	 * New customer with providing id.
	 */
	public boolean newCustomer(int id, int cid);

	/**
	 * Removes the customer and associated reservations.
	 */
	public boolean deleteCustomer(int id, int customer);

	/**
	 * Returns a bill
	 */
	public String queryCustomerInfo(int id, int customer);

}
