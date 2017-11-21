package customers.impl;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Calendar;
import java.util.Map.Entry;

import common.data.RMHashtable;
import common.locks.DeadlockException;
import common.locks.LockManager;
import common.locks.TrxnObj;
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
	private LockManager lockManager;

	public CustomerManagerImpl() {
		this.customers = new RMHashtable<>("data/customers/customers.data");
		this.lockManager = new LockManager();
	}

	@Override
	public int newCustomer(int id) throws DeadlockException {
		log("newCustomer(" + id + ") called");

		lockManager.lock(id, "customers", TrxnObj.WRITE);
		int cid = Integer.parseInt(String.valueOf(id) + String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND))
				+ String.valueOf(Math.round(Math.random() * 100 + 1)));
		customers.put(id, cid, new Customer(cid));
		log("newCustomer(" + id + ") returns ID=" + cid);
		return cid;
	}

	@Override
	public boolean newCustomer(int id, int cid) throws DeadlockException {
		log("newCustomer(" + id + ", " + cid + ") called");

		lockManager.lock(id, "customers", TrxnObj.WRITE);
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
	public boolean deleteCustomer(int id, int cid) throws DeadlockException {
		log("deleteCustomer(" + id + ", " + cid + ") called");

		lockManager.lock(id, "customers", TrxnObj.WRITE);
		return customers.remove(id, cid) != null;
	}

	@Override
	public String queryCustomerInfo(int id, int cid) throws DeadlockException {
		log("queryCustomerInfo(" + id + ", " + cid + ") called");

		lockManager.lock(id, "customers", TrxnObj.READ);
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
	public boolean reserve(int id, int cid, String manager, String itemId, int price) throws DeadlockException {
		log("reserve(" + id + ", " + cid + ", " + manager + ", " + itemId + ") called");

		lockManager.lock(id, "customer-" + cid, TrxnObj.WRITE);
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
	public boolean cancelReservation(int id, int cid, String manager, String itemId) throws DeadlockException {
		log("cancelReservation(" + id + ", " + cid + ", " + manager + ", " + itemId + ") called");

		lockManager.lock(id, "customer-" + cid, TrxnObj.WRITE);
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
	public String queryReservations(int id, int cid) throws DeadlockException {
		log("queryReservations(" + id + ", " + cid + ") called");

		lockManager.lock(id, "customer-" + cid, TrxnObj.READ);
		String reservations = null;
		Customer customer = customers.get(id, cid);

		if (customer != null) {
			reservations = customer.getReservations();
		}

		log("queryReservations(" + id + ", " + cid + ") returning " + reservations);
		return reservations;
	}

	@Override
	public void clearReservationsForItem(int id, String itemId) throws RemoteException, DeadlockException {
		for (Entry<Integer, Customer> entry : customers.entrySet()) {
			lockManager.lock(id, "customer-" + entry.getKey(), TrxnObj.WRITE);
			entry.getValue().clearReservationsForItem(id, itemId);
		}
	}

	@Override
	public boolean commit(int id) {
		log("Committing transaction " + id);
		lockManager.unlockAll(id);
		return true;
	}

	@Override
	public boolean abort(int id) {
		log("Aborting transaction " + id);

		lockManager.unlockAll(id);
		customers.cancel(id);

		for (Customer customer : customers.values()) {
			customer.cancel(id);
		}

		return true;
	}

	private void log(String message) {
		System.out.println("[CustomerManager] " + message);
	}

	@Override
	public boolean shutdown() throws RemoteException {
		// Since this manager runs on the middle ware server, this method is not
		// necessary.
		return false;
	}
}
