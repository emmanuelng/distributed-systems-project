package flights;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FlightManager extends Remote {

	/**
	 * Add seats to a flight. In general this will be used to create a new flight,
	 * but it should be possible to add seats to an existing flight. Adding to an
	 * existing flight should overwrite the current price of the available seats.
	 *
	 * @return success.
	 */
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice) throws RemoteException;

	/**
	 * Deletes the entire flight. Implies whole deletion of the flight. all seats,
	 * all reservations. If there is a reservation on the flight, then the flight
	 * cannot be deleted
	 *
	 * @return success.
	 */
	public boolean deleteFlight(int id, int flightNum) throws RemoteException;

	/**
	 * Returns the number of empty seats.
	 */
	public int queryFlight(int id, int flightNumber) throws RemoteException;

	/**
	 * Returns the price of a seat on this flight.
	 */
	public int queryFlightPrice(int id, int flightNumber) throws RemoteException;

	/**
	 * Reserves a seat on this flight.
	 * 
	 * @return success
	 */
	public boolean reserveFlight(int id, int flightNumber) throws RemoteException;

	/**
	 * Releases reserved seats in the given flight.
	 * 
	 * @return success
	 */
	public boolean releaseSeats(int id, int flightNumber, int amount) throws RemoteException;

	/**
	 * Starts a new transaction
	 * 
	 * @return success
	 */
	public boolean start(int id);

	/**
	 * Commits a transaction.
	 * 
	 * @return success
	 */
	public boolean commit(int id);

	/**
	 * Aborts a transaction.
	 * 
	 * @return success
	 */
	public boolean abort(int id);

}
