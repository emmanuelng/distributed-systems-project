package hotels.impl;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import common.locks.DeadlockException;
import common.reservations.ReservationManager;
import hotels.HotelManager;

@SuppressWarnings("deprecation")
public class HotelManagerImpl extends ReservationManager<Hotel> implements HotelManager {

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
			HotelManagerImpl obj = new HotelManagerImpl();
			HotelManager proxyObj = (HotelManager) UnicastRemoteObject.exportObject(obj, 0);

			// Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.getRegistry(port);
			registry.rebind("hotels.group20", proxyObj);

			System.out.println("Hotel server ready");
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}

		// Create and install a security manager
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}
	}

	public HotelManagerImpl() {
		super("hotels");
	}

	@Override
	public boolean addRooms(int id, String location, int numRooms, int price) throws DeadlockException {
		if (getItem(id, location) == null) {
			addItem(id, location, new Hotel(location, numRooms, price));
		} else {
			increaseItemCount(id, location, numRooms, price);
		}

		return true;
	}

	@Override
	public boolean deleteRooms(int id, String location) throws DeadlockException {
		return deleteItem(id, location);
	}

	@Override
	public int queryRooms(int id, String location) throws DeadlockException {
		return queryNum(id, location);
	}

	@Override
	public int queryRoomsPrice(int id, String location) throws DeadlockException {
		return queryPrice(id, location);
	}

	@Override
	public boolean reserveRoom(int id, String location) throws DeadlockException {
		return reserveItem(id, location);
	}

	@Override
	public boolean releaseRoom(int id, String location, int amount) throws DeadlockException {
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

}
