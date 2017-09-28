package customers.impl;

import java.rmi.RemoteException;
import java.util.Calendar;

import common.data.RMHashtable;
import customers.CustomerManager;

public class CustomerManagerImpl implements CustomerManager {

	private RMHashtable<Integer, Customer> customers;

	public CustomerManagerImpl() {
		this.customers = new RMHashtable<>();
	}

	@Override
	public int newCustomer(int id) {
		log("newCustomer(" + id + ") called");

		int cid = Integer.parseInt(String.valueOf(id) + String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND))
				+ String.valueOf(Math.round(Math.random() * 100 + 1)));
		customers.put(id, cid, new Customer(cid));

		log("newCustomer(" + id + ") returns ID=" + cid);
		return cid;
	}

	@Override
	public boolean newCustomer(int id, int cid) {
		log("newCustomer(" + id + ", " + cid + ") called");

		boolean success = true;
		Customer customer = customers.get(id, cid);

		if (customer == null) {
			customers.put(id, cid, new Customer(cid));
			log("newCustomer(" + id + ", " + cid + ") created a new customer");
		} else {
			log("newCustomer(" + id + ", " + cid + ") failed: customer already exist");
			success = false;
		}

		return success;
	}

	@Override
	public boolean deleteCustomer(int id, int cid) {
		log("deleteCustomer(" + id + ", " + cid + ") called");
		return customers.remove(id, cid) != null;
	}

	@Override
	public String queryCustomerInfo(int id, int cid) {
		log("queryCustomerInfo(" + id + ", " + cid + ") called");

		String bill = "";
		Customer customer = customers.get(id, cid);

		if (customer == null) {
			log("queryCustomerInfo(" + id + ", " + cid + ") failed: customer does not exist");
		} else {
			bill = customer.printBill();
			log("queryCustomerInfo(" + id + ", " + cid + ") succeeded, bills follows:\n" + bill);
		}

		return bill;
	}

	@Override
	public boolean reserve(int id, int cid, String manager, String itemId, int price) {
		log("reserve(" + id + ", " + cid + ", " + itemId + ") called");

		boolean success = true;
		Customer customer = customers.get(id, cid);

		if (customer == null) {
			success = false;
			log("reserve(" + id + ", " + cid + ", " + itemId + ") failed: customer does not exist");
		} else {
			customer.reserve(id, manager, itemId, price);
			log("reserve(" + id + ", " + cid + ", " + itemId + ") succeeded");
		}

		return success;
	}

	@Override
	public String[][] queryReservations(int id, int cid) {
		log("queryReservations(" + id + ", " + cid + ") called");

		String[][] reservations = null;
		Customer customer = customers.get(id, cid);

		if (customer != null) {
			reservations = customer.getReservations();
		}

		return reservations;
	}

	@Override
	public void clearReservationsForItem(int id, String itemId) throws RemoteException {
		for(Customer customer: customers.values()) {
			customer.clearReservationsForItem(id, itemId);
		}
	}

	private void log(String message) {
		System.out.println("[CustomerManager] " + message);
	}

}
