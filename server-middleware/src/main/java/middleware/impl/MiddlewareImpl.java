package middleware.impl;

import java.rmi.ConnectException;
import java.rmi.RMISecurityManager;
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
import middleware.impl.exceptions.TimeoutException;

/**
 * Implementation of the {@link Middleware} interface.
 */
@SuppressWarnings("deprecation")
public class MiddlewareImpl implements Middleware {

	public static void main(String[] args) {
		// Check the number of arguments
		if (args.length != 0 && args.length != 1 && args.length != 3 && args.length != 4) {
			System.out.println("Usage: middleware <carHost[:port]> <flightHost[:port]> <hotelHost[:port]> [port]");
			return;
		}

		try {
			// Parse the arguments
			String carServer = args.length >= 3 ? args[0] : "localhost:1099";
			String flightServer = args.length >= 3 ? args[1] : "localhost:1099";
			String hotelServer = args.length >= 3 ? args[2] : "localhost:1099";
			int port = args.length > 0 && args.length != 3 ? Integer.parseInt(args[args.length - 1]) : 1099;

			// Create a new server object and dynamically generate the stub (client proxy)
			MiddlewareImpl obj = new MiddlewareImpl(carServer, flightServer, hotelServer);
			Middleware proxyObj = (Middleware) UnicastRemoteObject.exportObject(obj, 0);

			// Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.getRegistry(port);
			registry.rebind("middleware.group20", proxyObj);

			System.out.println("Middleware ready");
		} catch (Exception e) {
			System.err.println("Error: " + e.toString());
			e.printStackTrace();
		}

		// Create and install a security manager
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}
	}

	private CarManager carManager;
	private FlightManager flightManager;
	private HotelManager hotelManager;
	private CustomerManager customerManager;
	private TransactionManager tm;

	private Map<String, ResourceManager> rms;
	private String carServer, flightServer, hotelServer;

	/**
	 * Initializes the middle ware.
	 * 
	 * @param carServer
	 *            the address of the cars server
	 * @param flightServer
	 *            the address of the flight server
	 * @param hotelServer
	 *            the address if the hotel server
	 */
	public MiddlewareImpl(String carServer, String flightServer, String hotelServer) {
		this.rms = new HashMap<>();

		this.carServer = carServer;
		this.flightServer = flightServer;
		this.hotelServer = hotelServer;

		this.carManager = (CarManager) connect("cars.group20", carServer);
		this.flightManager = (FlightManager) connect("flights.group20", flightServer);
		this.hotelManager = (HotelManager) connect("hotels.group20", hotelServer);
		this.customerManager = new CustomerManagerImpl();

		this.tm = new TransactionManager(this);
	}

	@Override
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice) throws RemoteException {
		checkTransaction(id);
		tm.enlist(id, flightManager);

		try {
			return flightManager.addFlight(id, flightNum, flightSeats, flightPrice);
		} catch (DeadlockException e) {
			abort(id);
		} catch (ConnectException e) {
			reconnect(flightManager);
			return addFlight(id, flightNum, flightSeats, flightPrice);
		}

		return false;
	}

	@Override
	public boolean addCars(int id, String location, int numCars, int price) throws RemoteException {
		checkTransaction(id);
		tm.enlist(id, carManager);

		try {
			return carManager.addCars(id, location, numCars, price);
		} catch (DeadlockException e) {
			abort(id);
			return false;
		} catch (ConnectException e) {
			reconnect(carManager);
			return addCars(id, location, numCars, price);
		}
	}

	@Override
	public boolean addRooms(int id, String location, int numRooms, int price) throws RemoteException {
		checkTransaction(id);

		try {
			tm.enlist(id, hotelManager);
			return hotelManager.addRooms(id, location, numRooms, price);
		} catch (DeadlockException e) {
			abort(id);
			return false;
		} catch (ConnectException e) {
			reconnect(hotelManager);
			return addRooms(id, location, numRooms, price);
		}
	}

	@Override
	public int newCustomer(int id) throws RemoteException {
		checkTransaction(id);

		try {
			tm.enlist(id, customerManager);
			return customerManager.newCustomer(id);
		} catch (DeadlockException e) {
			abort(id);
			return 0;
		} catch (ConnectException e) {
			reconnect(customerManager);
			return newCustomer(id);
		}
	}

	@Override
	public boolean newCustomer(int id, int cid) throws RemoteException {
		checkTransaction(id);

		try {
			tm.enlist(id, customerManager);
			return customerManager.newCustomer(id, cid);
		} catch (DeadlockException e) {
			abort(cid);
			return false;
		} catch (ConnectException e) {
			reconnect(customerManager);
			return newCustomer(id, cid);
		}
	}

	@Override
	public boolean deleteFlight(int id, int flightNum) throws RemoteException {
		checkTransaction(id);

		try {
			tm.enlist(id, flightManager);
			return flightManager.deleteFlight(id, flightNum);
		} catch (DeadlockException e) {
			abort(id);
			return false;
		} catch (ConnectException e) {
			reconnect(flightManager);
			return deleteFlight(id, flightNum);
		}
	}

	@Override
	public boolean deleteCars(int id, String location) throws RemoteException {
		checkTransaction(id);

		try {
			tm.enlist(id, carManager);
			return carManager.deleteCars(id, location);
		} catch (DeadlockException e) {
			abort(id);
			return false;
		} catch (ConnectException e) {
			reconnect(carManager);
			return deleteCars(id, location);
		}
	}

	@Override
	public boolean deleteRooms(int id, String location) throws RemoteException {
		checkTransaction(id);

		try {
			tm.enlist(id, hotelManager);
			return hotelManager.deleteRooms(id, location);
		} catch (DeadlockException e) {
			abort(id);
			return false;
		} catch (ConnectException e) {
			reconnect(hotelManager);
			return deleteRooms(id, location);
		}
	}

	@Override
	public boolean deleteCustomer(int id, int customer) throws RemoteException {
		checkTransaction(id);
		boolean success = false;

		try {
			// Release the items reserved by the customer, then remove the customer
			tm.enlist(id, customerManager);
			String reservations = customerManager.queryReservations(id, customer);

			if (reservations != null) {
				for (String reservation : reservations.split(";")) {
					log("Cancelling reservation " + reservation);
					String[] splitted = reservation.split("/");

					if (splitted.length == 3) {
						// Parse the reservation string
						String managerName = splitted[0];
						String itemId = splitted[1];
						int amount = Integer.parseInt(splitted[2]);

						// Call the right manager
						if (managerName.equals("cars")) {
							tm.enlist(id, carManager);
							carManager.releaseCars(id, itemId, amount);
						} else if (managerName.equals("flights")) {
							tm.enlist(id, flightManager);
							flightManager.releaseSeats(id, Integer.parseInt(itemId), amount);
						} else if (managerName.equals("hotels")) {
							tm.enlist(id, hotelManager);
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
		} catch (ConnectException e) {
			reconnect(customerManager);
			reconnect(flightManager);
			reconnect(carManager);
			reconnect(hotelManager);

			return deleteCustomer(id, customer);
		}
	}

	@Override
	public int queryFlight(int id, int flightNumber) throws RemoteException {
		checkTransaction(id);

		try {
			tm.enlist(id, flightManager);
			return flightManager.queryFlight(id, flightNumber);
		} catch (DeadlockException e) {
			abort(id);
			return 0;
		} catch (ConnectException e) {
			reconnect(flightManager);
			return queryFlight(id, flightNumber);
		}
	}

	@Override
	public int queryCars(int id, String location) throws RemoteException {
		checkTransaction(id);

		try {
			tm.enlist(id, carManager);
			return carManager.queryCars(id, location);
		} catch (DeadlockException e) {
			abort(id);
			return 0;
		} catch (ConnectException e) {
			reconnect(carManager);
			return queryCars(id, location);
		}
	}

	@Override
	public int queryRooms(int id, String location) throws RemoteException {
		checkTransaction(id);

		try {
			tm.enlist(id, hotelManager);
			return hotelManager.queryRooms(id, location);
		} catch (DeadlockException e) {
			abort(id);
			return 0;
		} catch (ConnectException e) {
			reconnect(hotelManager);
			return queryRooms(id, location);
		}
	}

	@Override
	public String queryCustomerInfo(int id, int customer) throws RemoteException {
		checkTransaction(id);

		try {
			tm.enlist(id, customerManager);
			return customerManager.queryCustomerInfo(id, customer);
		} catch (DeadlockException e) {
			abort(id);
			return null;
		} catch (ConnectException e) {
			reconnect(customerManager);
			return queryCustomerInfo(id, customer);
		}
	}

	@Override
	public int queryFlightPrice(int id, int flightNumber) throws RemoteException {
		checkTransaction(id);

		try {
			tm.enlist(id, flightManager);
			return flightManager.queryFlightPrice(id, flightNumber);
		} catch (DeadlockException e) {
			abort(id);
			return 0;
		} catch (ConnectException e) {
			reconnect(flightManager);
			return queryFlightPrice(id, flightNumber);
		}
	}

	@Override
	public int queryCarsPrice(int id, String location) throws RemoteException {
		checkTransaction(id);

		try {
			tm.enlist(id, carManager);
			return carManager.queryCarsPrice(id, location);
		} catch (DeadlockException e) {
			abort(id);
			return 0;
		} catch (ConnectException e) {
			reconnect(carManager);
			return queryCarsPrice(id, location);
		}
	}

	@Override
	public int queryRoomsPrice(int id, String location) throws RemoteException {
		checkTransaction(id);

		try {
			tm.enlist(id, hotelManager);
			return hotelManager.queryRoomsPrice(id, location);
		} catch (DeadlockException e) {
			abort(id);
			return 0;
		} catch (ConnectException e) {
			reconnect(hotelManager);
			return queryRoomsPrice(id, location);
		}
	}

	@Override
	public boolean reserveFlight(int id, int customer, int flightNumber) throws RemoteException {
		checkTransaction(id);

		try {
			tm.enlist(id, flightManager);
			if (flightManager.reserveFlight(id, flightNumber)) {
				int price = flightManager.queryFlightPrice(id, flightNumber);
				tm.enlist(id, customerManager);
				return customerManager.reserve(id, customer, "flights", flightNumber + "", price);
			}

			return false;
		} catch (DeadlockException e) {
			abort(id);
			return false;
		} catch (ConnectException e) {
			reconnect(flightManager);
			reconnect(customerManager);

			return reserveFlight(id, customer, flightNumber);
		}
	}

	@Override
	public boolean reserveCar(int id, int customer, String location) throws RemoteException {
		checkTransaction(id);

		try {
			tm.enlist(id, carManager);
			if (carManager.reserveCar(id, location)) {
				int price = carManager.queryCarsPrice(id, location);
				tm.enlist(id, customerManager);
				return customerManager.reserve(id, customer, "cars", location, price);
			}
		} catch (DeadlockException e) {
			abort(id);
		} catch (ConnectException e) {
			reconnect(carManager);
			reconnect(customerManager);

			return reserveCar(id, customer, location);
		}

		return false;
	}

	@Override
	public boolean reserveRoom(int id, int customer, String location) throws RemoteException {
		checkTransaction(id);

		try {
			tm.enlist(id, hotelManager);

			if (hotelManager.reserveRoom(id, location)) {
				int price = hotelManager.queryRoomsPrice(id, location);
				tm.enlist(id, customerManager);
				return customerManager.reserve(id, customer, "hotels", location, price);
			}

		} catch (DeadlockException e) {
			abort(id);
		} catch (ConnectException e) {
			reconnect(hotelManager);
			reconnect(customerManager);

			return reserveRoom(id, customer, location);
		}

		return false;
	}

	@Override
	public boolean itinerary(int id, int customer, Vector<Integer> flights, String location, boolean car, boolean room)
			throws RemoteException {
		checkTransaction(id);

		try {
			boolean success = true;

			Vector<Object> reservedFlights = new Vector<>();
			boolean carReserved = false, roomReserved = false;

			for (Integer flightNumber : flights) {
				if (reserveFlight(id, customer, flightNumber)) {
					reservedFlights.add(flightNumber);
				} else {
					success = false;
				}
			}

			if (success) {
				if (car) {
					carReserved = reserveCar(id, customer, location);
					success = carReserved;
				}

				if (room) {
					roomReserved = reserveRoom(id, customer, location);
					success = roomReserved;
				}

			} else {
				tm.enlist(id, customerManager);
				tm.enlist(id, flightManager);

				for (Object flightNumber : reservedFlights) {
					flightManager.releaseSeats(id, (int) flightNumber, 1);
					customerManager.cancelReservation(id, customer, "flights", (String) flightNumber);
				}

				if (carReserved) {
					tm.enlist(id, carManager);
					carManager.releaseCars(id, location, 1);
					customerManager.cancelReservation(id, customer, "cars", location);
				}

				if (roomReserved) {
					tm.enlist(id, hotelManager);
					hotelManager.releaseRoom(id, location, 1);
					customerManager.cancelReservation(id, customer, "hotels", location);
				}
			}

			return success;
		} catch (DeadlockException e) {
			abort(id);
			return false;
		} catch (ConnectException e) {
			reconnect(carManager);
			reconnect(flightManager);
			reconnect(hotelManager);
			reconnect(customerManager);

			return itinerary(id, customer, flights, location, car, room);
		}
	}

	@Override
	public int start() {
		return tm.startTransaction();
	}

	@Override
	public boolean prepare(int id) throws RemoteException {
		checkTransaction(id);
		return tm.prepareTransaction(id);
	}

	@Override
	public boolean commit(int id) throws RemoteException {
		return tm.commitTransaction(id);
	}

	@Override
	public boolean abort(int id) throws RemoteException {
		checkTransaction(id);
		return tm.abortTransaction(id);
	}

	@Override
	public boolean shutdown() throws RemoteException {
		if (tm.canShutDown()) {
			log("Shutting down managers...");
			boolean success = true;

			success &= shutdownRM(carManager);
			success &= shutdownRM(flightManager);
			success &= shutdownRM(hotelManager);

			return success & selfDestroy(0);
		}

		return false;
	}

	@Override
	public boolean crash(String which) throws RemoteException {
		boolean success = false;

		try {
			switch (which.toLowerCase()) {
			case "middleware":
				success = selfDestroy(1);
				break;
			case "cars":
				success = carManager.selfDestroy(1);
				break;
			case "flights":
				success = flightManager.selfDestroy(1);
				break;
			case "hotels":
				success = hotelManager.selfDestroy(1);
				break;
			default:
				break;
			}
		} catch (ConnectException e) {
			reconnect(carManager);
			reconnect(flightManager);
			reconnect(hotelManager);

			return crash(which);
		}

		return success;
	}

	boolean prepare(String rm, int id) throws RemoteException {
		try {
			ResourceManager resourceManager = rms.get(rm);
			return resourceManager.prepare(id);
		} catch (ConnectException e) {
			reconnect(rms.get(rm));
			return prepare(rm, id);
		}
	}

	/**
	 * Sends a commit request using the resource manager key.
	 */
	boolean commit(String rm, int id) throws RemoteException {
		try {
			ResourceManager resourceManager = rms.get(rm);
			return resourceManager.commit(id);
		} catch (ConnectException e) {
			reconnect(rms.get(rm));
			return commit(id);
		}
	}

	/**
	 * Sends an abort request using the resource manager key.
	 */
	boolean abort(String rm, int id) throws RemoteException {
		try {
			ResourceManager resourceManager = rms.get(rm);
			return resourceManager.abort(id);
		} catch (ConnectException e) {
			reconnect(rms.get(rm));
			return abort(id);
		}
	}

	/**
	 * Connects to a resource manager.
	 * 
	 * @param name
	 *            the key of the resource manager in the RMI registry
	 * @param address
	 *            the address of the resource manager (host:port)
	 */
	private ResourceManager connect(String name, String address) {
		ResourceManager rm = null;

		String[] splitted = address.split(":");
		String host = splitted[0];
		String portStr = splitted.length > 1 ? splitted[1] : "1099";

		try {
			int port = Integer.parseInt(portStr);

			Registry registry = LocateRegistry.getRegistry(host, port);
			rm = (ResourceManager) registry.lookup(name);
			rms.put(rm.getClass().getInterfaces()[0].getName(), rm);

		} catch (Exception e) {
			log("Unable to connect to " + host + ":" + portStr);
			System.exit(0);
		}

		return rm;
	}

	/**
	 * Reconnects to a resource manager.
	 */
	private ResourceManager reconnect(ResourceManager rm) {
		ResourceManager result = null;

		if (rm instanceof CarManager) {
			result = connect("cars.group20", carServer);
			carManager = (CarManager) result;
		} else if (rm instanceof FlightManager) {
			result = connect("flights.group20", flightServer);
			flightManager = (FlightManager) result;
		} else if (rm instanceof HotelManager) {
			result = connect("hotels.group20", hotelServer);
			hotelManager = (HotelManager) result;
		}

		return result;
	}

	/**
	 * Checks if the given id is a valid transaction id. If it is not valid, throws
	 * an exception.
	 */
	private void checkTransaction(int id) throws RemoteException {
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
	 * Shut downs a particular resource manager.
	 */
	private boolean shutdownRM(ResourceManager rm) throws RemoteException {
		try {
			rm.shutdown();
			return true;
		} catch (ConnectException e) {
			return shutdownRM(reconnect(rm));
		}
	}

	/**
	 * Starts a timer that stops the middle ware after one second.
	 * 
	 * @return success
	 */
	private boolean selfDestroy(int status) {
		log("Will shut down in 1 second.");

		Timer shutdownTimer = new Timer();
		shutdownTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				System.exit(status);
			}
		}, 1000);

		return true;
	}

	/**
	 * Writes a message to log.
	 */
	private void log(String message) {
		System.out.println("[Middleware] " + message);
	}
}
