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
		this.customers = new RMHashtable<>();
		this.lockManager = new LockManager();
	}

	@Override
	public int newCustomer(int id) throws DeadlockException {
        long startTime = System.nanoTime();
        System.out.println("[CustomerManager] newCustomer start " + id + ": " + startTime);
	    log("newCustomer(" + id + ") called");

		lockManager.lock(id, "customers", TrxnObj.WRITE);
		int cid = Integer.parseInt(String.valueOf(id) + String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND))
				+ String.valueOf(Math.round(Math.random() * 100 + 1)));
		customers.put(id, cid, new Customer(cid));
		log("newCustomer(" + id + ") returns ID=" + cid);
        long endTime = System.nanoTime();
        System.out.println("[CustomerManager] newCustomer end "+ id + ": " + endTime);
		return cid;
	}

	@Override
	public boolean newCustomer(int id, int cid) throws DeadlockException {
        long startTime = System.nanoTime();
        System.out.println("[CustomerManager] newCustomer start (cid) " + id + ": " + startTime);
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
        long endTime = System.nanoTime();
        System.out.println("[CustomerManager] newCustomer end (cid) "+ id + ": " + endTime);
		return success;
	}

	@Override
	public boolean deleteCustomer(int id, int cid) throws DeadlockException {
        long startTime = System.nanoTime();
        System.out.println("[CustomerManager] deleteCustomer start " + id + ": " + startTime);
		log("deleteCustomer(" + id + ", " + cid + ") called");

		lockManager.lock(id, "customers", TrxnObj.WRITE);
        long endTime = System.nanoTime();
        System.out.println("[CustomerManager] deleteCustomer end:"+ id + ": " + endTime);
		return customers.remove(id, cid) != null;
	}

	@Override
	public String queryCustomerInfo(int id, int cid) throws DeadlockException {
        long startTime = System.nanoTime();
        System.out.println("[CustomerManager] queryCustomer start " + id + ": " + startTime);
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
        long endTime = System.nanoTime();
        System.out.println("[CustomerManager] queryCustomer end:"+ id + ": " + endTime);
		return bill;
	}

	@Override
	public boolean reserve(int id, int cid, String manager, String itemId, int price) throws DeadlockException {
        long startTime = System.nanoTime();
        System.out.println("[CustomerManager] reserve start " + id + ": " + startTime);
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
        long endTime = System.nanoTime();
        System.out.println("[CustomerManager] reserve end:"+ id + ": " + endTime);
		return success;
	}

	@Override
	public boolean cancelReservation(int id, int cid, String manager, String itemId) throws DeadlockException {
        long startTime = System.nanoTime();
        System.out.println("[CustomerManager] cancelReservation start " + id + ": " + startTime);
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
        long endTime = System.nanoTime();
        System.out.println("[CustomerManager] cancelReservation end:"+ id + ": " + endTime);
		return success;
	}

	@Override
	public String queryReservations(int id, int cid) throws DeadlockException {
        long startTime = System.nanoTime();
        System.out.println("[CustomerManager] queryReservation start " + id + ": " + startTime);
		log("queryReservations(" + id + ", " + cid + ") called");

		lockManager.lock(id, "customer-" + cid, TrxnObj.READ);
		String reservations = null;
		Customer customer = customers.get(id, cid);

		if (customer != null) {
			reservations = customer.getReservations();
		}

		log("queryReservations(" + id + ", " + cid + ") returning " + reservations);
        long endTime = System.nanoTime();
        System.out.println("[CustomerManager] queryReservation end:"+ id + ": " + endTime);
		return reservations;
	}

	@Override
	public void clearReservationsForItem(int id, String itemId) throws RemoteException, DeadlockException {
        long startTime = System.nanoTime();
        System.out.println("[CustomerManager] clearReservation start " + id + ": " + startTime);
		for (Entry<Integer, Customer> entry : customers.entrySet()) {
			lockManager.lock(id, "customer-" + entry.getKey(), TrxnObj.WRITE);
			entry.getValue().clearReservationsForItem(id, itemId);
		}
        long endTime = System.nanoTime();
        System.out.println("[CustomerManager] clearReservation end:"+ id + ": " + endTime);
	}

	@Override
	public boolean commit(int id) {
        long startTime = System.nanoTime();
        System.out.println("[CustomerManager] commit start " + id + ": " + startTime);
		log("Committing transaction " + id);
		lockManager.unlockAll(id);
        long endTime = System.nanoTime();
        System.out.println("[CustomerManager] commit end:"+ id + ": " + endTime);
		return true;
	}

	@Override
	public boolean abort(int id) {
		long startTime = System.nanoTime();
        System.out.println("[CustomerManager] abort start " + id + ": " + startTime);
		log("Aborting transaction " + id );

		lockManager.unlockAll(id);
		customers.cancel(id);

		for (Customer customer : customers.values()) {
			customer.cancel(id);
		}
        long endTime = System.nanoTime();
		System.out.println("[CustomerManager] abort end:"+ id + ": " + endTime);
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
