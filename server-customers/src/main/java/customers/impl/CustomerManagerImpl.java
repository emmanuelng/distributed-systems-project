package customers.impl;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

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
	private Set<Integer> activeTransactions;

	public CustomerManagerImpl() {
		this.customers = new RMHashtable<>();
		this.lockManager = new LockManager();
		this.activeTransactions = new HashSet<>();
	}

	@Override
	public int newCustomer(int id) {
		log("newCustomer(" + id + ") called");
		int cid = Integer.parseInt(String.valueOf(id) + String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND))
				+ String.valueOf(Math.round(Math.random() * 100 + 1)));

		try {
			lockManager.lock(id, "customers", TrxnObj.WRITE);
			customers.put(id, cid, new Customer(cid));
			log("newCustomer(" + id + ") returns ID=" + cid);
			return cid;

		} catch (DeadlockException e) {
			abort(id);
			return -1;
		}
	}

	@Override
	public boolean newCustomer(int id, int cid) {
		log("newCustomer(" + id + ", " + cid + ") called");

		try {
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

		} catch (DeadlockException e) {
			abort(id);
			return false;
		}
	}

	@Override
	public boolean deleteCustomer(int id, int cid) {
		log("deleteCustomer(" + id + ", " + cid + ") called");

		try {
			lockManager.lock(id, "customers", TrxnObj.WRITE);
			return customers.remove(id, cid) != null;
		} catch (DeadlockException e) {
			abort(id);
			return false;
		}
	}

	@Override
	public String queryCustomerInfo(int id, int cid) {
		log("queryCustomerInfo(" + id + ", " + cid + ") called");

		try {
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

		} catch (DeadlockException e) {
			abort(id);
			return null;
		}

	}

	@Override
	public boolean reserve(int id, int cid, String manager, String itemId, int price) {
		log("reserve(" + id + ", " + cid + ", " + manager + ", " + itemId + ") called");

		try {
			lockManager.lock(id, "customer-" + cid, TrxnObj.WRITE);
			boolean success = true;
			Customer customer = customers.get(id, cid);

			if (customer == null) {
				success = false;
				log("reserve(" + id + ", " + cid + ", " + manager + ", " + itemId
						+ ") failed: customer does not exist");
			} else {
				customer.reserve(id, manager, itemId, price);
				log("reserve(" + id + ", " + cid + ", " + manager + ", " + itemId + ") succeeded");
			}

			return success;

		} catch (DeadlockException e) {
			abort(id);
			return false;
		}
	}

	@Override
	public boolean cancelReservation(int id, int cid, String manager, String itemId) {
		log("cancelReservation(" + id + ", " + cid + ", " + manager + ", " + itemId + ") called");

		try {
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

		} catch (DeadlockException e) {
			abort(id);
			return false;
		}
	}

	@Override
	public String queryReservations(int id, int cid) {
		log("queryReservations(" + id + ", " + cid + ") called");

		try {
			lockManager.lock(id, "customer-" + cid, TrxnObj.READ);
			String reservations = null;
			Customer customer = customers.get(id, cid);

			if (customer != null) {
				reservations = customer.getReservations();
			}

			log("queryReservations(" + id + ", " + cid + ") returning " + reservations);
			return reservations;

		} catch (DeadlockException e) {
			abort(id);
			return null;
		}
	}

	@Override
	public void clearReservationsForItem(int id, String itemId) throws RemoteException {
		for (Entry<Integer, Customer> entry : customers.entrySet()) {
			try {
				lockManager.lock(id, "customer-" + entry.getKey(), TrxnObj.WRITE);
				entry.getValue().clearReservationsForItem(id, itemId);
			} catch (DeadlockException e) {
				abort(id);
				return;
			}
		}
	}

	@Override
	public boolean start(int id) {
		log("Starting transaction " + id);
		return activeTransactions.add(id);
	}

	@Override
	public boolean commit(int id) {
		if (activeTransactions.contains(id)) {
			log("Committing transaction " + id);
			lockManager.unlockAll(id);
			activeTransactions.remove(id);
			return true;
		}

		return false;
	}

	@Override
	public boolean abort(int id) {
		if (activeTransactions.contains(id)) {
			log("Aborting transaction " + id);
			
			customers.cancel(id);
			for (Customer customer: customers.values()) {
				customer.cancel(id);
			}
			
			return true;
		}
		return false;
	}

	private void log(String message) {
		System.out.println("[CustomerManager] " + message);
	}
}
