package customers.impl;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Calendar;

import common.data.RMHashtable;
import customers.CustomerManager;

@SuppressWarnings("deprecation")
public class CustomerManagerImpl implements CustomerManager {

	public static void main(String[] args) {
		// Figure out where server is running
		String server = "localhost";
		int port = 1099;

		if (args.length == 1) {
			server = server + ":" + args[0];
			port = Integer.parseInt(args[0]);
		} else if (args.length != 0 && args.length != 1) {
			System.err.println("Wrong usage");
			System.exit(1);
		}

		try {
			// Create a new server object and dynamically generate the stub (client proxy)
			CustomerManagerImpl obj = new CustomerManagerImpl();
			CustomerManager proxyObj = (CustomerManager) UnicastRemoteObject.exportObject(obj, 0);

			// Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.getRegistry(port);
			registry.rebind("customers.group20", proxyObj);

			System.out.println("Customer server ready");
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}

		// Create and install a security manager
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}
	}

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
		log("reserve(" + id + ", " + cid + ", " + manager + ", " + itemId + ") called");

		boolean success = true;
		Customer customer = customers.get(id, cid);

		if (customer == null) {
			success = false;
			log("reserve(" + id + ", " + cid + ", " + manager + ", " + itemId + ") failed: customer does not exist");
		} else {
			customer.reserve(id, manager, itemId, price);
			log("reserve(" + id + ", " + cid + ", " + manager + ", " + itemId + ") succeeded");
		}

		return success;
	}

	@Override
	public boolean cancelReservation(int id, int cid, String manager, String itemId) {
		log("cancelReservation(" + id + ", " + cid + ", " + manager + ", " + itemId + ") called");

		boolean success = true;
		Customer customer = customers.get(id, cid);

		if (customer == null) {
			success = false;
			log("cancelReservation(" + id + ", " + cid + ", " + manager + ", " + itemId
					+ ") failed: customer does not exist");
		} else {
			customer.cancelReservation(id, manager, itemId);
			log("cancelReservation(" + id + ", " + cid + ", " + manager + ", " + itemId + ") succeeded");
		}

		return success;
	}

	@Override
	public String queryReservations(int id, int cid) {
		log("queryReservations(" + id + ", " + cid + ") called");

		String reservations = null;
		Customer customer = customers.get(id, cid);

		if (customer != null) {
			reservations = customer.getReservations();
		}

		log("queryReservations(" + id + ", " + cid + ") returning " + reservations);
		return reservations;
	}

	@Override
	public void clearReservationsForItem(int id, String itemId) throws RemoteException {
		for (Customer customer : customers.values()) {
			customer.clearReservationsForItem(id, itemId);
		}
	}

	@Override
	public boolean start(int id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean commit(int id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean abort(int id) {
		// TODO Auto-generated method stub
		return false;
	}

	private void log(String message) {
		System.out.println("[CustomerManager] " + message);
	}
}
