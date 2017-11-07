package cars;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CarManager extends Remote {

	/**
	 * Add cars to a location. This should look a lot like addFlight, only keyed on
	 * a string location instead of a flight number.
	 */
	public boolean addCars(int id, String location, int numCars, int price) throws RemoteException;

	/**
	 * Deletes all Cars from a location. It may not succeed if there are
	 * reservations for this location.
	 *
	 * @return success
	 */
	public boolean deleteCars(int id, String location) throws RemoteException;

	/**
	 * Return the number of cars available at a location.
	 */
	public int queryCars(int id, String location) throws RemoteException;

	/**
	 * Returns the price of a car at a location.
	 */
	public int queryCarsPrice(int id, String location) throws RemoteException;

	/**
	 * Reserves a car at this location.
	 * 
	 * @return success
	 */
	public boolean reserveCar(int id, String location) throws RemoteException;

	/**
	 * Releases previously reserved cars.
	 * 
	 * @return success
	 */
	public boolean releaseCars(int id, String location, int amount) throws RemoteException;

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
