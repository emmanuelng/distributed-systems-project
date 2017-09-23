package cars;

import java.rmi.Remote;

public interface CarManager extends Remote {

	/**
	 * Add cars to a location. This should look a lot like addFlight, only keyed on
	 * a string location instead of a flight number.
	 */
	boolean addCars(int id, String location, int numCars, int price);

	/**
	 * Deletes all Cars from a location. It may not succeed if there are
	 * reservations for this location.
	 *
	 * @return success
	 */
	boolean deleteCars(int id, String location);

	/**
	 * Return the number of cars available at a location.
	 */
	int queryCars(int id, String location);

	/**
	 * Returns the price of a car at a location.
	 */
	int queryCarsPrice(int id, String location);

	/**
	 * Reserves a car at this location.
	 */
	boolean reserveCar(int id, int customer, String location);

}
