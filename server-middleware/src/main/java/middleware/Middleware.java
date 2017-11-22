package middleware;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

import middleware.impl.InvalidTransactionException;
import middleware.impl.TimeoutException;

/**
 * Simplified version from CSE 593 University of Washington
 *
 * Distributed System in Java.
 * 
 * failure reporting is done using two pieces, exceptions and boolean return
 * values. Exceptions are used for systemy things. Return values are used for
 * operations that would affect the consistency
 * 
 * If there is a boolean return value and you're not sure how it would be used
 * in your implementation, ignore it. I used boolean return values in the
 * interface generously to allow flexibility in implementation. But don't forget
 * to return true when the operation has succeeded.
 */

public interface Middleware extends Remote {

	/**
	 * Add seats to a flight. In general this will be used to create a new flight,
	 * but it should be possible to add seats to an existing flight. Adding to an
	 * existing flight should overwrite the current price of the available seats.
	 *
	 * @return success.
	 */
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
			throws RemoteException, InvalidTransactionException, TimeoutException;

	/**
	 * Add cars to a location. This should look a lot like addFlight, only keyed on
	 * a string location instead of a flight number.
	 * 
	 * @throws InvalidTransactionException
	 * @throws TimeoutException
	 */
	public boolean addCars(int id, String location, int numCars, int price)
			throws RemoteException, InvalidTransactionException, TimeoutException;

	/**
	 * Add rooms to a location. This should look a lot like addFlight, only keyed on
	 * a string location instead of a flight number.
	 */
	public boolean addRooms(int id, String location, int numRooms, int price)
			throws RemoteException, InvalidTransactionException, TimeoutException;

	/**
	 * Returns a unique customer identifier.
	 */
	public int newCustomer(int id) throws RemoteException, InvalidTransactionException, TimeoutException;

	/**
	 * New customer with providing id.
	 */
	public boolean newCustomer(int id, int cid) throws RemoteException, InvalidTransactionException, TimeoutException;

	/**
	 * Deletes the entire flight. Implies whole deletion of the flight. all seats,
	 * all reservations. If there is a reservation on the flight, then the flight
	 * cannot be deleted
	 *
	 * @return success
	 */
	public boolean deleteFlight(int id, int flightNum)
			throws RemoteException, InvalidTransactionException, TimeoutException;

	/**
	 * Deletes all Cars from a location. It may not succeed if there are
	 * reservations for this location.
	 *
	 * @return success
	 */
	public boolean deleteCars(int id, String location)
			throws RemoteException, InvalidTransactionException, TimeoutException;

	/**
	 * Deletes all Rooms from a location. It may not succeed if there are
	 * reservations for this location.
	 *
	 * @return success
	 */
	public boolean deleteRooms(int id, String location)
			throws RemoteException, InvalidTransactionException, TimeoutException;

	/**
	 * Removes the customer and associated reservations.
	 */
	public boolean deleteCustomer(int id, int customer)
			throws RemoteException, InvalidTransactionException, TimeoutException;

	/**
	 * Returns the number of empty seats.
	 */
	public int queryFlight(int id, int flightNumber)
			throws RemoteException, InvalidTransactionException, TimeoutException;

	/**
	 * Return the number of cars available at a location.
	 */
	public int queryCars(int id, String location) throws RemoteException, InvalidTransactionException, TimeoutException;

	/**
	 * Returns the number of rooms available at a location
	 */
	public int queryRooms(int id, String location)
			throws RemoteException, InvalidTransactionException, TimeoutException;

	/**
	 * Returns a bill
	 */
	public String queryCustomerInfo(int id, int customer)
			throws RemoteException, InvalidTransactionException, TimeoutException;

	/**
	 * Returns the price of a seat on this flight.
	 */
	public int queryFlightPrice(int id, int flightNumber)
			throws RemoteException, InvalidTransactionException, TimeoutException;

	/**
	 * Returns the price of a car at a location.
	 */
	public int queryCarsPrice(int id, String location)
			throws RemoteException, InvalidTransactionException, TimeoutException;

	/**
	 * Returns the price of a room at a location.
	 */
	public int queryRoomsPrice(int id, String location)
			throws RemoteException, InvalidTransactionException, TimeoutException;

	/**
	 * Reserves a seat on this flight.
	 */
	public boolean reserveFlight(int id, int customer, int flightNumber)
			throws RemoteException, InvalidTransactionException, TimeoutException;

	/**
	 * Reserves a car at this location.
	 */
	public boolean reserveCar(int id, int customer, String location)
			throws RemoteException, InvalidTransactionException, TimeoutException;

	/**
	 * Reserves a room certain at this location.
	 */
	public boolean reserveRoom(int id, int customer, String location)
			throws RemoteException, InvalidTransactionException, TimeoutException;

	/**
	 * Reserves an itinerary.
	 */
	public boolean itinerary(int id, int customer, Vector<Integer> flightNumbers, String location, boolean car,
			boolean room) throws RemoteException, InvalidTransactionException, TimeoutException;

	/**
	 * Starts a new transaction.
	 * 
	 * @return the transaction id
	 */
	public int start() throws RemoteException;

	/**
	 * Initiates the first phase of two-phase commit (2PC)
	 */
	public boolean prepare(int id) throws RemoteException, InvalidTransactionException, TimeoutException;

	/**
	 * Commits a transaction.
	 */
	public boolean commit(int id) throws RemoteException, InvalidTransactionException, TimeoutException;

	/**
	 * Aborts a transaction.
	 */
	public boolean abort(int id) throws RemoteException, InvalidTransactionException, TimeoutException;

	/**
	 * Shutdowns the system gracefully.
	 */
	public boolean shutdown() throws RemoteException;

	/**
	 * Simulates a crash of a server.
	 * 
	 * @param which
	 *            the name of the server ("cars", "flights", "hotels" or "middleware")
	 */
	public boolean crash(String which) throws RemoteException;

}
