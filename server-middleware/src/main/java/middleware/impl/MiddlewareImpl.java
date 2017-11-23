package middleware.impl;

import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import cars.CarManager;
import common.locks.DeadlockException;
import common.rm.ResourceManager;
import customers.CustomerManager;
import customers.impl.CustomerManagerImpl;
import flights.FlightManager;
import hotels.HotelManager;
import middleware.Middleware;
import middleware.impl.exceptions.InvalidTransactionException;
import middleware.impl.exceptions.NotPreparedException;
import middleware.impl.exceptions.TimeoutException;

/**
 * Implementation of the {@link Middleware} interface.
 */
@SuppressWarnings("deprecation")
public class MiddlewareImpl implements Middleware {

	public static enum RM {
		CARS, FLIGHTS, HOTELS, CUSTOMERS
	}

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

	private Map<RM, ResourceManager> rms;
	private TransactionManager tm;

	public MiddlewareImpl(CarManager carManager, FlightManager flightManager, HotelManager hotelManager) {
		this.rms = new HashMap<>();
		this.tm = new TransactionManager(this);

		rms.put(RM.CARS, carManager);
		rms.put(RM.FLIGHTS, flightManager);
		rms.put(RM.HOTELS, hotelManager);
		rms.put(RM.CUSTOMERS, new CustomerManagerImpl());
	}

	public ResourceManager rm(RM key) {
		return rms.get(key);
	}

	@Override
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
			throws RemoteException, InvalidTransactionException, TimeoutException {
		FlightManager flightManager = (FlightManager) rm(RM.FLIGHTS);
		checkTransaction(id);
		tm.enlist(id, RM.FLIGHTS);

		try {
			return flightManager.addFlight(id, flightNum, flightSeats, flightPrice);
		} catch (DeadlockException e) {
			abort(id);
			return false;
		}
	}

	@Override
	public boolean addCars(int id, String location, int numCars, int price)
			throws RemoteException, InvalidTransactionException, TimeoutException {
		CarManager carManager = (CarManager) rm(RM.CARS);
		checkTransaction(id);
		tm.enlist(id, RM.CARS);

		try {
			return carManager.addCars(id, location, numCars, price);
		} catch (DeadlockException e) {
			abort(id);
			return false;
		}
	}

	@Override
	public boolean addRooms(int id, String location, int numRooms, int price)
			throws RemoteException, InvalidTransactionException, TimeoutException {
		HotelManager hotelManager = (HotelManager) rm(RM.HOTELS);
		checkTransaction(id);

		try {
			tm.enlist(id, RM.HOTELS);
			return hotelManager.addRooms(id, location, numRooms, price);
		} catch (DeadlockException e) {
			abort(id);
			return false;
		}
	}

	@Override
	public int newCustomer(int id) throws RemoteException, InvalidTransactionException, TimeoutException {
		CustomerManager customerManager = (CustomerManager) rm(RM.CUSTOMERS);
		checkTransaction(id);

		try {
			tm.enlist(id, RM.CUSTOMERS);
			return customerManager.newCustomer(id);
		} catch (DeadlockException e) {
			abort(id);
			return 0;
		}
	}

	@Override
	public boolean newCustomer(int id, int cid) throws RemoteException, InvalidTransactionException, TimeoutException {
		CustomerManager customerManager = (CustomerManager) rm(RM.CUSTOMERS);
		checkTransaction(id);

		try {
			tm.enlist(id, RM.CUSTOMERS);
			return customerManager.newCustomer(id, cid);
		} catch (DeadlockException e) {
			abort(cid);
			return false;
		}
	}

	@Override
	public boolean deleteFlight(int id, int flightNum)
			throws RemoteException, InvalidTransactionException, TimeoutException {
		FlightManager flightManager = (FlightManager) rm(RM.FLIGHTS);
		checkTransaction(id);

		try {
			tm.enlist(id, RM.FLIGHTS);
			return flightManager.deleteFlight(id, flightNum);
		} catch (DeadlockException e) {
			abort(id);
			return false;
		}
	}

	@Override
	public boolean deleteCars(int id, String location)
			throws RemoteException, InvalidTransactionException, TimeoutException {
		CarManager carManager = (CarManager) rm(RM.CARS);
		checkTransaction(id);

		try {
			tm.enlist(id, RM.CARS);
			return carManager.deleteCars(id, location);
		} catch (DeadlockException e) {
			abort(id);
			return false;
		}
	}

	@Override
	public boolean deleteRooms(int id, String location)
			throws RemoteException, InvalidTransactionException, TimeoutException {
		HotelManager hotelManager = (HotelManager) rm(RM.HOTELS);
		checkTransaction(id);

		try {
			tm.enlist(id, RM.HOTELS);
			return hotelManager.deleteRooms(id, location);
		} catch (DeadlockException e) {
			abort(id);
			return false;
		}
	}

	@Override
	public boolean deleteCustomer(int id, int customer)
			throws RemoteException, InvalidTransactionException, TimeoutException {

		CarManager carManager = (CarManager) rm(RM.CARS);
		FlightManager flightManager = (FlightManager) rm(RM.FLIGHTS);
		HotelManager hotelManager = (HotelManager) rm(RM.HOTELS);

		checkTransaction(id);
		boolean success = false;

		try {
			// Release the items reserved by the customer, then remove the customer
			CustomerManager customerManager = (CustomerManager) rm(RM.CUSTOMERS);
			tm.enlist(id, RM.CUSTOMERS);
			String reservations = customerManager.queryReservations(id, customer);

			if (reservations != null) {
				for (String reservation : reservations.split(";")) {
					System.out.println("[Middleware] Cancelling reservation " + reservation);
					String[] splitted = reservation.split("/");

					if (splitted.length == 3) {
						// Parse the reservation string
						String managerName = splitted[0];
						String itemId = splitted[1];
						int amount = Integer.parseInt(splitted[2]);

						// Call the right manager
						if (managerName.equals("cars")) {
							tm.enlist(id, RM.CARS);
							carManager.releaseCars(id, itemId, amount);
						} else if (managerName.equals("flights")) {
							tm.enlist(id, RM.FLIGHTS);
							flightManager.releaseSeats(id, Integer.parseInt(itemId), amount);
						} else if (managerName.equals("hotels")) {
							tm.enlist(id, RM.HOTELS);
							hotelManager.releaseRoom(id, itemId, amount);
						}

					}
				}

				// Now that all reservations are cleared, delete the customer
				customerManager.deleteCustomer(id, customer);
				success = true;
			}

			return success;
		} catch (DeadlockException e) {
			abort(id);
			return false;
		}
	}

	@Override
	public int queryFlight(int id, int flightNumber)
			throws RemoteException, InvalidTransactionException, TimeoutException {
		FlightManager flightManager = (FlightManager) rm(RM.FLIGHTS);
		checkTransaction(id);

		try {
			tm.enlist(id, RM.FLIGHTS);
			return flightManager.queryFlight(id, flightNumber);
		} catch (DeadlockException e) {
			abort(id);
			return 0;
		}
	}

	@Override
	public int queryCars(int id, String location)
			throws RemoteException, InvalidTransactionException, TimeoutException {
		CarManager carManager = (CarManager) rm(RM.CARS);
		checkTransaction(id);

		try {
			tm.enlist(id, RM.CARS);
			return carManager.queryCars(id, location);
		} catch (DeadlockException e) {
			abort(id);
			return 0;
		}
	}

	@Override
	public int queryRooms(int id, String location)
			throws RemoteException, InvalidTransactionException, TimeoutException {
		HotelManager hotelManager = (HotelManager) rm(RM.HOTELS);
		checkTransaction(id);

		try {
			tm.enlist(id, RM.HOTELS);
			return hotelManager.queryRooms(id, location);
		} catch (DeadlockException e) {
			abort(id);
			return 0;
		}
	}

	@Override
	public String queryCustomerInfo(int id, int customer)
			throws RemoteException, InvalidTransactionException, TimeoutException {
		CustomerManager customerManager = (CustomerManager) rm(RM.CUSTOMERS);
		checkTransaction(id);

		try {
			tm.enlist(id, RM.CUSTOMERS);
			return customerManager.queryCustomerInfo(id, customer);
		} catch (DeadlockException e) {
			abort(id);
			return null;
		}
	}

	@Override
	public int queryFlightPrice(int id, int flightNumber)
			throws RemoteException, InvalidTransactionException, TimeoutException {
		FlightManager flightManager = (FlightManager) rm(RM.FLIGHTS);
		checkTransaction(id);

		try {
			tm.enlist(id, RM.FLIGHTS);
			return flightManager.queryFlightPrice(id, flightNumber);
		} catch (DeadlockException e) {
			abort(id);
			return 0;
		}
	}

	@Override
	public int queryCarsPrice(int id, String location)
			throws RemoteException, InvalidTransactionException, TimeoutException {
		CarManager carManager = (CarManager) rm(RM.CARS);
		checkTransaction(id);

		try {
			tm.enlist(id, RM.CARS);
			return carManager.queryCarsPrice(id, location);
		} catch (DeadlockException e) {
			abort(id);
			return 0;
		}
	}

	@Override
	public int queryRoomsPrice(int id, String location)
			throws RemoteException, InvalidTransactionException, TimeoutException {
		HotelManager hotelManager = (HotelManager) rm(RM.HOTELS);
		checkTransaction(id);

		try {
			tm.enlist(id, RM.HOTELS);
			return hotelManager.queryRoomsPrice(id, location);
		} catch (DeadlockException e) {
			abort(id);
			return 0;
		}
	}

	@Override
	public boolean reserveFlight(int id, int customer, int flightNumber)
			throws RemoteException, InvalidTransactionException, TimeoutException {
		FlightManager flightManager = (FlightManager) rm(RM.FLIGHTS);
		CustomerManager customerManager = (CustomerManager) rm(RM.CUSTOMERS);
		checkTransaction(id);

		try {
			tm.enlist(id, RM.FLIGHTS);
			if (flightManager.reserveFlight(id, flightNumber)) {
				int price = flightManager.queryFlightPrice(id, flightNumber);
				tm.enlist(id, RM.CUSTOMERS);
				return customerManager.reserve(id, customer, "flights", flightNumber + "", price);
			}

			return false;
		} catch (DeadlockException e) {
			abort(id);
			return false;
		}
	}

	@Override
	public boolean reserveCar(int id, int customer, String location)
			throws RemoteException, InvalidTransactionException, TimeoutException {

		CarManager carManager = (CarManager) rm(RM.CARS);
		CustomerManager customerManager = (CustomerManager) rm(RM.CUSTOMERS);
		checkTransaction(id);

		try {

			tm.enlist(id, RM.CARS);
			if (carManager.reserveCar(id, location)) {
				int price = carManager.queryCarsPrice(id, location);
				tm.enlist(id, RM.CUSTOMERS);
				return customerManager.reserve(id, customer, "cars", location, price);
			}

		} catch (DeadlockException e) {
			abort(id);
		}

		return false;
	}

	@Override
	public boolean reserveRoom(int id, int customer, String location)
			throws RemoteException, InvalidTransactionException, TimeoutException {

		HotelManager hotelManager = (HotelManager) rm(RM.HOTELS);
		CustomerManager customerManager = (CustomerManager) rm(RM.CUSTOMERS);
		checkTransaction(id);

		try {

			tm.enlist(id, RM.HOTELS);
			if (hotelManager.reserveRoom(id, location)) {
				int price = hotelManager.queryRoomsPrice(id, location);
				tm.enlist(id, RM.CUSTOMERS);
				return customerManager.reserve(id, customer, "hotels", location, price);
			}

		} catch (DeadlockException e) {
			abort(id);
		}

		return false;
	}

	@Override
	public boolean itinerary(int id, int customer, Vector<Integer> flightNumbers, String location, boolean car,
			boolean room) throws RemoteException, InvalidTransactionException, TimeoutException {

		FlightManager flightManager = (FlightManager) rm(RM.FLIGHTS);
		CarManager carManager = (CarManager) rm(RM.CARS);
		HotelManager hotelManager = (HotelManager) rm(RM.HOTELS);
		CustomerManager customerManager = (CustomerManager) rm(RM.CUSTOMERS);

		checkTransaction(id);

		try {
			boolean success = true;

			Vector<Object> reservedFlights = new Vector<>();
			boolean carReserved = false, roomReserved = false;

			for (Integer flightNumber : flightNumbers) {
				if (!reserveFlight(id, customer, flightNumber)) {
					success = false;
				} else {
					reservedFlights.add(flightNumber);
				}
			}

			if (success) {
				if (car) {
					if (reserveCar(id, customer, location)) {
						carReserved = true;
					} else {
						success = false;
					}
				}

				if (room) {
					if (reserveRoom(id, customer, location)) {
						roomReserved = true;
					} else {
						success = false;
					}
				}

			} else {
				tm.enlist(id, RM.CUSTOMERS);

				tm.enlist(id, RM.FLIGHTS);
				for (Object flightNumber : reservedFlights) {
					flightManager.releaseSeats(id, (int) flightNumber, 1);
					customerManager.cancelReservation(id, customer, "flights", (String) flightNumber);
				}

				if (carReserved) {
					tm.enlist(id, RM.CARS);
					carManager.releaseCars(id, location, 1);
					customerManager.cancelReservation(id, customer, "cars", location);
				}

				if (roomReserved) {
					tm.enlist(id, RM.HOTELS);
					hotelManager.releaseRoom(id, location, 1);
					customerManager.cancelReservation(id, customer, "hotels", location);
				}
			}

			return success;
		} catch (DeadlockException e) {
			abort(id);
			return false;
		}
	}

	@Override
	public int start() {
		return tm.startTransaction();
	}

	@Override
	public boolean prepare(int id) throws InvalidTransactionException, TimeoutException {
		checkTransaction(id);
		return tm.prepareTransaction(id);
	}

	@Override
	public boolean commit(int id) throws InvalidTransactionException, NotPreparedException {
		return tm.commitTransaction(id);
	}

	@Override
	public boolean abort(int id) throws InvalidTransactionException, TimeoutException {
		checkTransaction(id);
		return tm.abortTransaction(id);
	}

	@Override
	public boolean shutdown() throws RemoteException {
		if (tm.canShutDown()) {
			System.out.println("[Middleware] Shutting down managers...");
			boolean success = true;

			FlightManager flightManager = (FlightManager) rm(RM.FLIGHTS);
			CarManager carManager = (CarManager) rm(RM.CARS);
			HotelManager hotelManager = (HotelManager) rm(RM.HOTELS);

			success &= carManager.shutdown();
			success &= flightManager.shutdown();
			success &= hotelManager.shutdown();

			return success & selfDestroy();
		}

		return false;
	}

	@Override
	public boolean crash(String which) throws RemoteException {

		FlightManager flightManager = (FlightManager) rm(RM.FLIGHTS);
		CarManager carManager = (CarManager) rm(RM.CARS);
		HotelManager hotelManager = (HotelManager) rm(RM.HOTELS);

		boolean success = false;

		switch (which.toLowerCase()) {
		case "middleware":
			success = selfDestroy();
			break;
		case "cars":
			success = carManager.shutdown();
			break;
		case "flights":
			success = flightManager.shutdown();
			break;
		case "hotels":
			success = hotelManager.shutdown();
			break;
		default:
			break;
		}

		return success;
	}

	/**
	 * Checks if the given id is a valid transaction id. If it is not valid, throws
	 * an exception.
	 * 
	 * @throws InvalidTransactionException
	 * @throws TimeoutException
	 */
	private void checkTransaction(int id) throws InvalidTransactionException, TimeoutException {
		switch (tm.getStatus(id)) {
		case ACTIVE:
		case PREPARED:
			tm.resetTimeout(id);
			break;
		case COMMITTED:
			throw new InvalidTransactionException("The transaction was already committed");
		case ABORTED:
			throw new InvalidTransactionException("The transaction was aborted");
		case TIMED_OUT:
			throw new TimeoutException();
		default:
			throw new InvalidTransactionException("Invalid transaction id");
		}
	}

	/**
	 * Starts a timer that stops the middle ware after one second.
	 * 
	 * @return success
	 */
	private boolean selfDestroy() {
		System.out.println("[Middleware] Will shut down in 1 second.");

		Timer shutdownTimer = new Timer();
		shutdownTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				System.exit(0);
			}
		}, 1000);

		return true;
	}
}
