package flights.impl;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import common.locks.DeadlockException;
import common.reservations.ReservationManager;
import flights.FlightManager;

@SuppressWarnings("deprecation")
public class FlightManagerImpl extends ReservationManager<Flight> implements FlightManager {

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
			FlightManagerImpl obj = new FlightManagerImpl();
			FlightManager proxyObj = (FlightManager) UnicastRemoteObject.exportObject(obj, 0);

			// Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.getRegistry(port);
			registry.rebind("flights.group20", proxyObj);

			System.out.println("Flight server ready");
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
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice) throws DeadlockException {
		if (getItem(id, Integer.toString(flightNum)) == null) {
			addItem(id, Integer.toString(flightNum), new Flight(flightNum, flightSeats, flightPrice));
		} else {
			increaseItemCount(id, Integer.toString(flightNum), flightSeats, flightPrice);
		}

		return true;
	}

	@Override
	public boolean deleteFlight(int id, int flightNum) throws DeadlockException {
		return deleteItem(id, Integer.toString(flightNum));
	}

	@Override
	public int queryFlight(int id, int flightNum) throws DeadlockException {
		return queryNum(id, Integer.toString(flightNum));
	}

	@Override
	public int queryFlightPrice(int id, int flightNum) throws DeadlockException {
		return queryPrice(id, Integer.toString(flightNum));
	}

	@Override
	public boolean reserveFlight(int id, int flightNum) throws DeadlockException {
		return reserveItem(id, Integer.toString(flightNum));
	}

	@Override
	public boolean releaseSeats(int id, int flightNumber, int amount) throws RemoteException, DeadlockException {
		return increaseItemCount(id, Integer.toString(flightNumber), amount, 0);
	}

	@Override
	public boolean start(int id) {
		return startTransaction(id);
	}

	@Override
	public boolean commit(int id) {
		return commitTransaction(id);
	}

	@Override
	public boolean abort(int id) {
		return abortTransaction(id);
	}

}
