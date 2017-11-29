package cars;

import java.rmi.RemoteException;

import common.locks.DeadlockException;
import common.rm.ResourceManager;

public interface CarManager extends ResourceManager {

	/**
	 * Add cars to a location. This should look a lot like addFlight, only keyed on
	 * a string location instead of a flight number.
	 */
	public boolean addCars(int id, String location, int numCars, int price) throws RemoteException, DeadlockException;

	/**
	 * Deletes all Cars from a location. It may not succeed if there are
	 * reservations for this location.
	 *
	 * @return success
	 */
	public boolean deleteCars(int id, String location) throws RemoteException, DeadlockException;

	/**
	 * Return the number of cars available at a location.
	 */
	public int queryCars(int id, String location) throws RemoteException, DeadlockException;

	/**
	 * Returns the price of a car at a location.
	 */
	public int queryCarsPrice(int id, String location) throws RemoteException, DeadlockException;

	/**
	 * Reserves a car at this location.
	 * 
	 * @return success
	 */
	public boolean reserveCar(int id, String location) throws RemoteException, DeadlockException;

	/**
	 * Releases previously reserved cars.
	 * 
	 * @return success
	 */
	public boolean releaseCars(int id, String location, int amount) throws RemoteException, DeadlockException;

	/**
	 * Injects a crash in the manager.
	 */
	public boolean injectCrash(String when, String operation) throws RemoteException;
}
