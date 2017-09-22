package middleware.impl;

import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

import cars.CarManager;
import flights.FlightManager;
import hotels.HotelManager;
import middleware.Middleware;

/**
 * Implementation of the {@link Middleware} interface.
 */
@SuppressWarnings("deprecation")
public class MiddlewareImpl implements Middleware {

	public static void main(String[] args) {
		// Figure out where server is running
		int port = 1099;

		// Servers and ports ([0]: cars, [1]: flights, [2]: hotels)
		String[] servers = new String[3];
		int[] ports = { 1099, 1099, 1099 };

		if (args.length != 3 && args.length != 6 && args.length != 4 && args.length != 7) {
			System.err.println("Usage: middleware [cars server] [flight server] [hotel server]\n"
					+ "\tor middleware [cars server] [flight server] [hotel server] [middleware port]\n"
					+ "\tor middleware [car server] [flight server] [hotel server] [car port] [flight port] [hotel port]\n"
					+ "\tor middleware [car server] [flight server] [hotel server] [car port] [flight port] [hotel port] [middleware port]\n");
			System.exit(1);
		} else {
			servers[0] = args[0];
			servers[1] = args[1];
			servers[2] = args[2];

			if (args.length == 6) {
				ports[0] = Integer.parseInt(args[3]);
				ports[1] = Integer.parseInt(args[4]);
				ports[2] = Integer.parseInt(args[5]);
			}

			if (args.length == 4 || args.length == 7) {
				port = Integer.parseInt(args[args.length - 1]);
			}
		}

		try {
			// Find the managers
			CarManager carManager = (CarManager) lookup(servers[0], ports[0], "cars.group20");
			FlightManager flightManager = (FlightManager) lookup(servers[1], ports[1], "flights.group20");
			HotelManager hotelManager = (HotelManager) lookup(servers[2], ports[2], "hotels.group20");

			// Create a new server object and dynamically generate the stub (client proxy)
			MiddlewareImpl obj = new MiddlewareImpl(carManager, flightManager, hotelManager);
			Middleware proxyObj = (Middleware) UnicastRemoteObject.exportObject(obj, 0);

			// Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.getRegistry(port);
			registry.rebind("middleware.group20", proxyObj);

			System.out.println("Server ready");
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}

		// Create and install a security manager
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}
	}

	private static Remote lookup(String server, int port, String name) {
		Remote remoteObj = null;

		try {
			Registry registry = LocateRegistry.getRegistry(server, port);
			remoteObj = registry.lookup(name);

			if (remoteObj == null) {
				System.err.println("Error: Unsuccessful connection to " + name);
				System.exit(1);
			}

		} catch (RemoteException | NotBoundException e) {
			System.err.println("Error: Unable to locate " + name + " on " + server);
			System.exit(1);
		}

		return remoteObj;
	}

	private CarManager carManager;
	private FlightManager flightManager;
	private HotelManager hotelManager;

	public MiddlewareImpl(CarManager carManager, FlightManager flightManager, HotelManager hotelManager) {
		this.carManager = carManager;
		this.flightManager = flightManager;
		this.hotelManager = hotelManager;
	}

	@Override
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addCars(int id, String location, int numCars, int price) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addRooms(int id, String location, int numRooms, int price) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int newCustomer(int id) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean newCustomer(int id, int cid) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteFlight(int id, int flightNum) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteCars(int id, String location) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteRooms(int id, String location) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteCustomer(int id, int customer) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int queryFlight(int id, int flightNumber) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int queryCars(int id, String location) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int queryRooms(int id, String location) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String queryCustomerInfo(int id, int customer) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int queryFlightPrice(int id, int flightNumber) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int queryCarsPrice(int id, String location) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int queryRoomsPrice(int id, String location) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean reserveFlight(int id, int customer, int flightNumber) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reserveCar(int id, int customer, String location) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reserveRoom(int id, int customer, String locationd) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean itinerary(int id, int customer, Vector flightNumbers, String location, boolean Car, boolean Room)
			throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

}
