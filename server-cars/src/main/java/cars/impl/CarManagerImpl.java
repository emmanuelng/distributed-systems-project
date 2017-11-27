package cars.impl;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import cars.CarManager;
import common.locks.DeadlockException;
import common.reservations.ReservationManager;

@SuppressWarnings("deprecation")
public class CarManagerImpl extends ReservationManager<Car> implements CarManager {

	public static void main(String[] args) {
		// Figure out where server is running
		int port = 1099;

		if (args.length == 1) {
			port = Integer.parseInt(args[0]);
		} else if (args.length != 0 && args.length != 1) {
			System.err.println("Wrong usage");
			System.exit(1);
		}

		try {
			// Create a new server object and dynamically generate the stub (client proxy)
			CarManagerImpl obj = new CarManagerImpl();
			CarManager proxyObj = (CarManager) UnicastRemoteObject.exportObject(obj, 0);

			// Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.getRegistry(port);
			registry.rebind("cars.group20", proxyObj);

			System.out.println("Car server ready");
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}

		// Create and install a security manager
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}
	}

	public CarManagerImpl() {
		super("cars");
	}

	@Override
	public boolean addCars(int id, String location, int numCars, int price) throws DeadlockException {
		if (getItem(id, location) == null) {
			addItem(id, location, new Car(location, numCars, price));
		} else {
			increaseItemCount(id, location, numCars, price);
		}

		return true;
	}

	@Override
	public boolean deleteCars(int id, String location) throws DeadlockException {
		return deleteItem(id, location);
	}

	@Override
	public int queryCars(int id, String location) throws DeadlockException {
		return queryNum(id, location);
	}

	@Override
	public int queryCarsPrice(int id, String location) throws DeadlockException {
		return queryPrice(id, location);
	}

	@Override
	public boolean reserveCar(int id, String location) throws DeadlockException {
		return reserveItem(id, location);
	}

	@Override
	public boolean releaseCars(int id, String location, int amount) throws DeadlockException {
		return increaseItemCount(id, location, amount, 0);
	}

	@Override
	public boolean prepare(int id) {
		return prepareCommit(id);
	}

	@Override
	public boolean commit(int id) {
		return commitTransaction(id);
	}

	@Override
	public boolean abort(int id) {
		return abortTransaction(id);
	}

	@Override
	public boolean shutdown() throws RemoteException {
		return shutdownManager();
	}

	@Override
	public boolean selfDestroy(int status) {
		return selfDestroyManager(status);
	}

}
