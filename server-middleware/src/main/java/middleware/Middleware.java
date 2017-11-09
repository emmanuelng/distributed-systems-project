package middleware;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

import middleware.impl.InvalidTransactionException;

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
	 * @throws InvalidTransactionException
	 */
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
			throws RemoteException, InvalidTransactionException;

	/**
	 * Add cars to a location. This should look a lot like addFlight, only keyed on
	 * a string location instead of a flight number.
	 * 
	 * @throws InvalidTransactionException
	 */
	public boolean addCars(int id, String location, int numCars, int price)
			throws RemoteException, InvalidTransactionException;

	/**
	 * Add rooms to a location. This should look a lot like addFlight, only keyed on
	 * a string location instead of a flight number.
	 * 
	 * @throws InvalidTransactionException
	 */
	public boolean addRooms(int id, String location, int numRooms, int price)
			throws RemoteException, InvalidTransactionException;

	/**
	 * Returns a unique customer identifier.
	 * 
	 * @throws InvalidTransactionException
	 */
	public int newCustomer(int id) throws RemoteException, InvalidTransactionException;

	/**
	 * New customer with providing id.
	 * 
	 * @throws InvalidTransactionException
	 */
	public boolean newCustomer(int id, int cid) throws RemoteException, InvalidTransactionException;

	/**
	 * Deletes the entire flight. Implies whole deletion of the flight. all seats,
	 * all reservations. If there is a reservation on the flight, then the flight
	 * cannot be deleted
	 *
	 * @return success.
	 * @throws InvalidTransactionException
	 */
	public boolean deleteFlight(int id, int flightNum) throws RemoteException, InvalidTransactionException;

	/**
	 * Deletes all Cars from a location. It may not succeed if there are
	 * reservations for this location.
	 *
	 * @return success
	 * @throws InvalidTransactionException
	 */
	public boolean deleteCars(int id, String location) throws RemoteException, InvalidTransactionException;

	/**
	 * Deletes all Rooms from a location. It may not succeed if there are
	 * reservations for this location.
	 *
	 * @return success
	 * @throws InvalidTransactionException
	 */
	public boolean deleteRooms(int id, String location) throws RemoteException, InvalidTransactionException;

	/**
	 * Removes the customer and associated reservations.
	 * 
	 * @throws InvalidTransactionException
	 */
	public boolean deleteCustomer(int id, int customer) throws RemoteException, InvalidTransactionException;

	/**
	 * Returns the number of empty seats.
	 * 
	 * @throws InvalidTransactionException
	 */
	public int queryFlight(int id, int flightNumber) throws RemoteException, InvalidTransactionException;

	/**
	 * Return the number of cars available at a location.
	 * 
	 * @throws InvalidTransactionException
	 */
	public int queryCars(int id, String location) throws RemoteException, InvalidTransactionException;

	/**
	 * Returns the number of rooms available at a location
	 * 
	 * @throws InvalidTransactionException
	 */
	public int queryRooms(int id, String location) throws RemoteException, InvalidTransactionException;

	/**
	 * Returns a bill
	 * 
	 * @throws InvalidTransactionException
	 */
	public String queryCustomerInfo(int id, int customer) throws RemoteException, InvalidTransactionException;

	/**
	 * Returns the price of a seat on this flight.
	 * 
	 * @throws InvalidTransactionException
	 */
	public int queryFlightPrice(int id, int flightNumber) throws RemoteException, InvalidTransactionException;

	/**
	 * Returns the price of a car at a location.
	 * 
	 * @throws InvalidTransactionException
	 */
	public int queryCarsPrice(int id, String location) throws RemoteException, InvalidTransactionException;

	/**
	 * Returns the price of a room at a location.
	 * 
	 * @throws InvalidTransactionException
	 */
	public int queryRoomsPrice(int id, String location) throws RemoteException, InvalidTransactionException;

	/**
	 * Reserves a seat on this flight.
	 * 
	 * @throws InvalidTransactionException
	 */
	public boolean reserveFlight(int id, int customer, int flightNumber)
			throws RemoteException, InvalidTransactionException;

	/**
	 * Reserves a car at this location.
	 * 
	 * @throws InvalidTransactionException
	 */
	public boolean reserveCar(int id, int customer, String location)
			throws RemoteException, InvalidTransactionException;

	/**
	 * Reserves a room certain at this location.
	 * 
	 * @throws InvalidTransactionException
	 */
	public boolean reserveRoom(int id, int customer, String location)
			throws RemoteException, InvalidTransactionException;

	/**
	 * Reserves an itinerary.
	 * 
	 * @throws InvalidTransactionException
	 */
	public boolean itinerary(int id, int customer, Vector<Integer> flightNumbers, String location, boolean car,
			boolean room) throws RemoteException, InvalidTransactionException;

	/**
	 * Starts a new transaction.
	 * 
	 * @return the transaction id
	 */
	public int start();

	/**
	 * Commits a transaction.
	 * 
	 * @throws InvalidTransactionException
	 */
	public boolean commit(int id) throws InvalidTransactionException;

	/**
	 * Aborts a transaction.
	 * 
	 * @throws InvalidTransactionException
	 */
	public boolean abort(int id) throws InvalidTransactionException;

}
