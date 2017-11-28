package flights;

import java.rmi.RemoteException;

import common.locks.DeadlockException;
import common.rm.ResourceManager;

public interface FlightManager extends ResourceManager {

	/**
	 * Add seats to a flight. In general this will be used to create a new flight,
	 * but it should be possible to add seats to an existing flight. Adding to an
	 * existing flight should overwrite the current price of the available seats.
	 *
	 * @return success.
	 */
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
			throws RemoteException, DeadlockException;

	/**
	 * Deletes the entire flight. Implies whole deletion of the flight. all seats,
	 * all reservations. If there is a reservation on the flight, then the flight
	 * cannot be deleted
	 *
	 * @return success.
	 */
	public boolean deleteFlight(int id, int flightNum) throws RemoteException, DeadlockException;

	/**
	 * Returns the number of empty seats.
	 */
	public int queryFlight(int id, int flightNumber) throws RemoteException, DeadlockException;

	/**
	 * Returns the price of a seat on this flight.
	 */
	public int queryFlightPrice(int id, int flightNumber) throws RemoteException, DeadlockException;

	/**
	 * Reserves a seat on this flight.
	 * 
	 * @return success
	 */
	public boolean reserveFlight(int id, int flightNumber) throws RemoteException, DeadlockException;

	/**
	 * Releases reserved seats in the given flight.
	 * 
	 * @return success
	 */
	public boolean releaseSeats(int id, int flightNumber, int amount) throws RemoteException, DeadlockException;
}
