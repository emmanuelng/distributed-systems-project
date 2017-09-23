package flights;

import java.rmi.Remote;

public interface FlightManager extends Remote {

	/**
	 * Add seats to a flight. In general this will be used to create a new flight,
	 * but it should be possible to add seats to an existing flight. Adding to an
	 * existing flight should overwrite the current price of the available seats.
	 *
	 * @return success.
	 */
	boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice);

	/**
	 * Deletes the entire flight. Implies whole deletion of the flight. all seats,
	 * all reservations. If there is a reservation on the flight, then the flight
	 * cannot be deleted
	 *
	 * @return success.
	 */
	boolean deleteFlight(int id, int flightNum);

	/**
	 * Returns the number of empty seats.
	 */
	int queryFlight(int id, int flightNumber);

	/**
	 * Returns the price of a seat on this flight.
	 */
	int queryFlightPrice(int id, int flightNumber);

	/**
	 * Reserves a seat on this flight.
	 */
	boolean reserveFlight(int id, int customer, int flightNumber);

}
