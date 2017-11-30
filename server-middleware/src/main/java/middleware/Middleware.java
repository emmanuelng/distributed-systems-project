package middleware;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

import middleware.impl.exceptions.ServerException;

public interface Middleware extends Remote {

	/**
	 * Add seats to a flight. In general this will be used to create a new flight,
	 * but it should be possible to add seats to an existing flight. Adding to an
	 * existing flight should overwrite the current price of the available seats.
	 *
	 * @return success.
	 */
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
			throws RemoteException, ServerException;

	/**
	 * Add cars to a location. This should look a lot like addFlight, only keyed on
	 * a string location instead of a flight number.
	 */
	public boolean addCars(int id, String location, int numCars, int price) throws RemoteException, ServerException;

	/**
	 * Add rooms to a location. This should look a lot like addFlight, only keyed on
	 * a string location instead of a flight number.
	 */
	public boolean addRooms(int id, String location, int numRooms, int price) throws RemoteException, ServerException;

	/**
	 * Returns a unique customer identifier.
	 */
	public int newCustomer(int id) throws RemoteException, ServerException;

	/**
	 * New customer with providing id.
	 */
	public boolean newCustomer(int id, int cid) throws RemoteException, ServerException;

	/**
	 * Deletes the entire flight. Implies whole deletion of the flight. all seats,
	 * all reservations. If there is a reservation on the flight, then the flight
	 * cannot be deleted
	 *
	 * @return success
	 */
	public boolean deleteFlight(int id, int flightNum) throws RemoteException, ServerException;

	/**
	 * Deletes all Cars from a location. It may not succeed if there are
	 * reservations for this location.
	 *
	 * @return success
	 */
	public boolean deleteCars(int id, String location) throws RemoteException, ServerException;

	/**
	 * Deletes all Rooms from a location. It may not succeed if there are
	 * reservations for this location.
	 *
	 * @return success
	 */
	public boolean deleteRooms(int id, String location) throws RemoteException, ServerException;

	/**
	 * Removes the customer and associated reservations.
	 */
	public boolean deleteCustomer(int id, int customer) throws RemoteException, ServerException;

	/**
	 * Returns the number of empty seats.
	 */
	public int queryFlight(int id, int flightNumber) throws RemoteException, ServerException;

	/**
	 * Return the number of cars available at a location.
	 */
	public int queryCars(int id, String location) throws RemoteException, ServerException;

	/**
	 * Returns the number of rooms available at a location
	 */
	public int queryRooms(int id, String location) throws RemoteException, ServerException;

	/**
	 * Returns a bill
	 */
	public String queryCustomerInfo(int id, int customer) throws RemoteException, ServerException;

	/**
	 * Returns the price of a seat on this flight.
	 */
	public int queryFlightPrice(int id, int flightNumber) throws RemoteException, ServerException;

	/**
	 * Returns the price of a car at a location.
	 */
	public int queryCarsPrice(int id, String location) throws RemoteException, ServerException;

	/**
	 * Returns the price of a room at a location.
	 */
	public int queryRoomsPrice(int id, String location) throws RemoteException, ServerException;

	/**
	 * Reserves a seat on this flight.
	 */
	public boolean reserveFlight(int id, int customer, int flightNumber) throws RemoteException, ServerException;

	/**
	 * Reserves a car at this location.
	 */
	public boolean reserveCar(int id, int customer, String location) throws RemoteException, ServerException;

	/**
	 * Reserves a room certain at this location.
	 */
	public boolean reserveRoom(int id, int customer, String location) throws RemoteException, ServerException;

	/**
	 * Reserves an itinerary.
	 */
	public boolean itinerary(int id, int customer, Vector<Integer> flightNumbers, String location, boolean car,
			boolean room) throws RemoteException, ServerException;

	/**
	 * Starts a new transaction.
	 * 
	 * @return the transaction id
	 */
	public int start() throws RemoteException;

	/**
	 * Initiates the first phase of two-phase commit (2PC)
	 */
	public boolean prepare(int id) throws RemoteException, ServerException;

	/**
	 * Commits a transaction.
	 */
	public boolean commit(int id) throws RemoteException, ServerException;

	/**
	 * Aborts a transaction.
	 */
	public boolean abort(int id) throws RemoteException, ServerException;

	/**
	 * Shutdowns the system gracefully.
	 */
	public boolean shutdown() throws RemoteException;

	/**
	 * Simulates a crash of a server.
	 * 
	 * @param which
	 *            the name of the server ("cars", "flights", "hotels" or
	 *            "middleware")
	 */
	public boolean crash(String which) throws RemoteException;

	/**
	 * Injects a crash in the system at a precise place. When this place is reached,
	 * crashes the system. If this method is called twice with the same "where", the
	 * previous injection is overwritten.
	 * 
	 * @param where
	 *            one of "middleware", "cars", "flights", "hotels", "customers"
	 * @param when
	 *            one of "before", "in", "after"
	 * @param operation
	 *            one of "prepare", "decision"
	 */
	public boolean injectCrash(String where, String when, String operation) throws RemoteException;

}
