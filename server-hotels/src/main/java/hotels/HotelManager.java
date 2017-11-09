package hotels;

import java.rmi.Remote;
import java.rmi.RemoteException;

import common.locks.DeadlockException;
import common.transactions.TransactionHandler;

public interface HotelManager extends Remote, TransactionHandler {

	/**
	 * Add rooms to a location. This should look a lot like addFlight, only keyed on
	 * a string location instead of a flight number.
	 * 
	 * @throws DeadlockException
	 */
	public boolean addRooms(int id, String location, int numRooms, int price) throws RemoteException, DeadlockException;

	/**
	 * Deletes all Rooms from a location. It may not succeed if there are
	 * reservations for this location.
	 *
	 * @return success
	 * @throws DeadlockException
	 */
	public boolean deleteRooms(int id, String location) throws RemoteException, DeadlockException;

	/**
	 * Returns the number of rooms available at a location
	 * 
	 * @throws DeadlockException
	 */
	public int queryRooms(int id, String location) throws RemoteException, DeadlockException;

	/**
	 * Returns the price of a room at a location.
	 * 
	 * @throws DeadlockException
	 */
	public int queryRoomsPrice(int id, String location) throws RemoteException, DeadlockException;

	/**
	 * Reserves a room certain at this location.
	 * 
	 * @throws DeadlockException
	 */
	public boolean reserveRoom(int id, String location) throws RemoteException, DeadlockException;

	/**
	 * Releases a reserved room.
	 * 
	 * @return success
	 * @throws DeadlockException
	 */
	public boolean releaseRoom(int id, String location, int amount) throws RemoteException, DeadlockException;

}
