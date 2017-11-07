package hotels;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface HotelManager extends Remote {

	/**
	 * Add rooms to a location. This should look a lot like addFlight, only keyed on
	 * a string location instead of a flight number.
	 */
	public boolean addRooms(int id, String location, int numRooms, int price) throws RemoteException;

	/**
	 * Deletes all Rooms from a location. It may not succeed if there are
	 * reservations for this location.
	 *
	 * @return success
	 */
	public boolean deleteRooms(int id, String location) throws RemoteException;

	/**
	 * Returns the number of rooms available at a location
	 */
	public int queryRooms(int id, String location) throws RemoteException;

	/**
	 * Returns the price of a room at a location.
	 */
	public int queryRoomsPrice(int id, String location) throws RemoteException;

	/**
	 * Reserves a room certain at this location.
	 */
	public boolean reserveRoom(int id, String location) throws RemoteException;

	/**
	 * Releases a reserved room.
	 * 
	 * @return success
	 */
	public boolean releaseRoom(int id, String location, int amount) throws RemoteException;

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
