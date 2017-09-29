package hotels;

import java.rmi.Remote;

public interface HotelManager extends Remote {

	/**
	 * Add rooms to a location. This should look a lot like addFlight, only keyed on
	 * a string location instead of a flight number.
	 */
	public boolean addRooms(int id, String location, int numRooms, int price);

	/**
	 * Deletes all Rooms from a location. It may not succeed if there are
	 * reservations for this location.
	 *
	 * @return success
	 */
	public boolean deleteRooms(int id, String location);

	/**
	 * Returns the number of rooms available at a location
	 */
	public int queryRooms(int id, String location);

	/**
	 * Returns the price of a room at a location.
	 */
	public int queryRoomsPrice(int id, String location);

	/**
	 * Reserves a room certain at this location.
	 */
	public boolean reserveRoom(int id, String location);

	/**
	 * Releases a reserved room.
	 * 
	 * @return success
	 */
	public boolean releaseRoom(int id, String location, int amount);

}
