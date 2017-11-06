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
import customers.CustomerManager;
import customers.impl.CustomerManagerImpl;
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

	/**
	 * Finds the remote object registered with the given name on the given server.
	 * If the object does not exist, stops the program.
	 * 
	 * @param server
	 *            the server
	 * @param port
	 *            the registry port number
	 * @param name
	 *            the logical name of the remote object
	 * @return the object
	 */
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
	private CustomerManager customerManager;

	public MiddlewareImpl(CarManager carManager, FlightManager flightManager, HotelManager hotelManager) {
		this.carManager = carManager;
		this.flightManager = flightManager;
		this.hotelManager = hotelManager;

		// The customers are handled in the middle ware server
		this.customerManager = new CustomerManagerImpl();
	}

	@Override
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice) throws RemoteException {
		return flightManager.addFlight(id, flightNum, flightSeats, flightPrice);
	}

	@Override
	public boolean addCars(int id, String location, int numCars, int price) throws RemoteException {
		return carManager.addCars(id, location, numCars, price);
	}

	@Override
	public boolean addRooms(int id, String location, int numRooms, int price) throws RemoteException {
		return hotelManager.addRooms(id, location, numRooms, price);
	}

	@Override
	public int newCustomer(int id) throws RemoteException {
		return customerManager.newCustomer(id);
	}

	@Override
	public boolean newCustomer(int id, int cid) throws RemoteException {
		return customerManager.newCustomer(id, cid);
	}

	@Override
	public boolean deleteFlight(int id, int flightNum) throws RemoteException {
		return flightManager.deleteFlight(id, flightNum);
	}

	@Override
	public boolean deleteCars(int id, String location) throws RemoteException {
		return carManager.deleteCars(id, location);
	}

	@Override
	public boolean deleteRooms(int id, String location) throws RemoteException {
		return hotelManager.deleteRooms(id, location);
	}

	@Override
	public boolean deleteCustomer(int id, int customer) throws RemoteException {
		boolean success = true;

		// Release the items reserved by the customer, then remove the customer
		String reservationsStr = customerManager.queryReservations(id, customer);
		if (reservationsStr != null) {

			String[] reservationStrArray = reservationsStr.split(";");
			String[][] reservationsToRemove = new String[reservationStrArray.length][3];
			int i = 0;

			for (String reservation : reservationsStr.split(";")) {
				reservationsToRemove[i] = reservation.split("/");
				i++;
			}

			for (String[] reservation : reservationsToRemove) {
				// A reservation array has the format [manager, itemId, amount]
				String managerName = reservation[0];
				String itemId = reservation[1];
				int amount = Integer.parseInt(reservation[2]);

				// Call the right manager
				if (managerName.equals("cars")) {
					carManager.releaseCars(id, itemId, amount);
				} else if (managerName.equals("flights")) {
					flightManager.releaseSeats(id, Integer.parseInt(itemId), amount);
				} else if (managerName.equals("hotels")) {
					hotelManager.releaseRoom(id, itemId, amount);
				}
			}

			// Now that all reservations are cleared, delete the customer
			customerManager.deleteCustomer(id, customer);
		} else {
			success = false;
		}

		return success;
	}

	@Override
	public int queryFlight(int id, int flightNumber) throws RemoteException {
		return flightManager.queryFlight(id, flightNumber);
	}

	@Override
	public int queryCars(int id, String location) throws RemoteException {
		return carManager.queryCars(id, location);
	}

	@Override
	public int queryRooms(int id, String location) throws RemoteException {
		return hotelManager.queryRooms(id, location);
	}

	@Override
	public String queryCustomerInfo(int id, int customer) throws RemoteException {
		return customerManager.queryCustomerInfo(id, customer);
	}

	@Override
	public int queryFlightPrice(int id, int flightNumber) throws RemoteException {
		return flightManager.queryFlightPrice(id, flightNumber);
	}

	@Override
	public int queryCarsPrice(int id, String location) throws RemoteException {
		return carManager.queryCarsPrice(id, location);
	}

	@Override
	public int queryRoomsPrice(int id, String location) throws RemoteException {
		return hotelManager.queryRoomsPrice(id, location);
	}

	@Override
	public boolean reserveFlight(int id, int customer, int flightNumber) throws RemoteException {
		if (flightManager.reserveFlight(id, flightNumber)) {
			int price = flightManager.queryFlightPrice(id, flightNumber);
			return customerManager.reserve(id, customer, "flights", flightNumber + "", price);
		}

		return false;
	}

	@Override
	public boolean reserveCar(int id, int customer, String location) throws RemoteException {
		if (carManager.reserveCar(id, location)) {
			int price = carManager.queryCarsPrice(id, location);
			return customerManager.reserve(id, customer, "cars", location, price);
		}

		return false;
	}

	@Override
	public boolean reserveRoom(int id, int customer, String location) throws RemoteException {
		if (hotelManager.reserveRoom(id, location)) {
			int price = hotelManager.queryRoomsPrice(id, location);
			return customerManager.reserve(id, customer, "hotels", location, price);
		}

		return false;
	}

	@Override
	public boolean itinerary(int id, int customer, Vector<Object> flightNumbers, String location, boolean car,
			boolean room) throws RemoteException {
		boolean success = true;

		Vector<Object> reservedFlights = new Vector<>();
		boolean carReserved = false, roomReserved = false;

		for (Object flightNumber : flightNumbers) {
			if (!reserveFlight(id, customer, Integer.parseInt((String) flightNumber))) {
				success = false;
			} else {
				reservedFlights.add(flightNumber);
			}
		}

		if (success && car) {
			if (reserveCar(id, customer, location)) {
				carReserved = true;
			} else {
				success = false;
			}
		}

		if (success && room) {
			if (reserveRoom(id, customer, location)) {
				roomReserved = true;
			} else {
				success = false;
			}
		}

		if (!success) {
			for (Object flightNumber : reservedFlights) {
				flightManager.releaseSeats(id, (int) flightNumber, 1);
				customerManager.cancelReservation(id, customer, "flights", (String) flightNumber);
			}

			if (carReserved) {
				carManager.releaseCars(id, location, 1);
				customerManager.cancelReservation(id, customer, "cars", location);
			}

			if (roomReserved) {
				hotelManager.releaseRoom(id, location, 1);
				customerManager.cancelReservation(id, customer, "hotels", location);
			}
		}

		return success;
	}

}
