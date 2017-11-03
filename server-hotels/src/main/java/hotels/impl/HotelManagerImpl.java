package hotels.impl;

import java.rmi.RMISecurityManager;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

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

	@Override
	public boolean addRooms(int id, String location, int numRooms, int price) {
		if (getItem(id, location) == null) {
			addItem(id, location, new Hotel(location, numRooms, price));
		} else {
			increaseItemCount(id, location, numRooms, price);
		}

		return true;
	}

	@Override
	public boolean deleteRooms(int id, String location) {
		return deleteItem(id, location);
	}

	@Override
	public int queryRooms(int id, String location) {
		return queryNum(id, location);
	}

	@Override
	public int queryRoomsPrice(int id, String location) {
		return queryPrice(id, location);
	}

	@Override
	public boolean reserveRoom(int id, String location) {
		return reserveItem(id, location);
	}

	@Override
	public boolean releaseRoom(int id, String location, int amount) {
		return increaseItemCount(id, location, amount, 0);
	}

}