package middleware;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

import middleware.impl.exceptions.InvalidTransactionException;
import middleware.impl.exceptions.NotPreparedException;
import middleware.impl.exceptions.TransactionTimeoutException;

public interface Middleware extends Remote {

	/**
	 * Add seats to a flight. In general this will be used to create a new flight,
	 * but it should be possible to add seats to an existing flight. Adding to an
	 * existing flight should overwrite the current price of the available seats.
	 *
	 * @return success.
	 */
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
			throws RemoteException, InvalidTransactionException, TransactionTimeoutException;

	/**
	 * Add cars to a location. This should look a lot like addFlight, only keyed on
	 * a string location instead of a flight number.
	 */
	public boolean addCars(int id, String location, int numCars, int price)
			throws RemoteException, InvalidTransactionException, TransactionTimeoutException;

	/**
	 * Add rooms to a location. This should look a lot like addFlight, only keyed on
	 * a string location instead of a flight number.
	 */
	public boolean addRooms(int id, String location, int numRooms, int price)
			throws RemoteException, InvalidTransactionException, TransactionTimeoutException;

	/**
	 * Returns a unique customer identifier.
	 */
	public int newCustomer(int id) throws RemoteException, InvalidTransactionException, TransactionTimeoutException;

	/**
	 * New customer with providing id.
	 */
	public boolean newCustomer(int id, int cid) throws RemoteException, InvalidTransactionException, TransactionTimeoutException;

	/**
	 * Deletes the entire flight. Implies whole deletion of the flight. all seats,
	 * all reservations. If there is a reservation on the flight, then the flight
	 * cannot be deleted
	 *
	 * @return success
	 */
	public boolean deleteFlight(int id, int flightNum)
			throws RemoteException, InvalidTransactionException, TransactionTimeoutException;

	/**
	 * Deletes all Cars from a location. It may not succeed if there are
	 * reservations for this location.
	 *
	 * @return success
	 */
	public boolean deleteCars(int id, String location)
			throws RemoteException, InvalidTransactionException, TransactionTimeoutException;

	/**
	 * Deletes all Rooms from a location. It may not succeed if there are
	 * reservations for this location.
	 *
	 * @return success
	 */
	public boolean deleteRooms(int id, String location)
			throws RemoteException, InvalidTransactionException, TransactionTimeoutException;

	/**
	 * Removes the customer and associated reservations.
	 */
	public boolean deleteCustomer(int id, int customer)
			throws RemoteException, InvalidTransactionException, TransactionTimeoutException;

	/**
	 * Returns the number of empty seats.
	 */
	public int queryFlight(int id, int flightNumber)
			throws RemoteException, InvalidTransactionException, TransactionTimeoutException;

	/**
	 * Return the number of cars available at a location.
	 */
	public int queryCars(int id, String location) throws RemoteException, InvalidTransactionException, TransactionTimeoutException;

	/**
	 * Returns the number of rooms available at a location
	 */
	public int queryRooms(int id, String location)
			throws RemoteException, InvalidTransactionException, TransactionTimeoutException;

	/**
	 * Returns a bill
	 */
	public String queryCustomerInfo(int id, int customer)
			throws RemoteException, InvalidTransactionException, TransactionTimeoutException;

	/**
	 * Returns the price of a seat on this flight.
	 */
	public int queryFlightPrice(int id, int flightNumber)
			throws RemoteException, InvalidTransactionException, TransactionTimeoutException;

	/**
	 * Returns the price of a car at a location.
	 */
	public int queryCarsPrice(int id, String location)
			throws RemoteException, InvalidTransactionException, TransactionTimeoutException;

	/**
	 * Returns the price of a room at a location.
	 */
	public int queryRoomsPrice(int id, String location)
			throws RemoteException, InvalidTransactionException, TransactionTimeoutException;

	/**
	 * Reserves a seat on this flight.
	 */
	public boolean reserveFlight(int id, int customer, int flightNumber)
			throws RemoteException, InvalidTransactionException, TransactionTimeoutException;

	/**
	 * Reserves a car at this location.
	 */
	public boolean reserveCar(int id, int customer, String location)
			throws RemoteException, InvalidTransactionException, TransactionTimeoutException;

	/**
	 * Reserves a room certain at this location.
	 */
	public boolean reserveRoom(int id, int customer, String location)
			throws RemoteException, InvalidTransactionException, TransactionTimeoutException;

	/**
	 * Reserves an itinerary.
	 */
	public boolean itinerary(int id, int customer, Vector<Integer> flightNumbers, String location, boolean car,
			boolean room) throws RemoteException, InvalidTransactionException, TransactionTimeoutException;

	/**
	 * Starts a new transaction.
	 * 
	 * @return the transaction id
	 */
	public int start() throws RemoteException;

	/**
	 * Initiates the first phase of two-phase commit (2PC)
	 */
	public boolean prepare(int id) throws RemoteException, InvalidTransactionException, TransactionTimeoutException;

	/**
	 * Commits a transaction.
	 */
	public boolean commit(int id)
			throws RemoteException, InvalidTransactionException, TransactionTimeoutException, NotPreparedException;

	/**
	 * Aborts a transaction.
	 */
	public boolean abort(int id) throws RemoteException, InvalidTransactionException, TransactionTimeoutException;

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

}
