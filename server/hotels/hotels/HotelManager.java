package hotels;

import java.rmi.Remote;

public interface HotelManager extends Remote {

	/**
	 * Add rooms to a location. This should look a lot like addFlight, only keyed on
	 * a string location instead of a flight number.
	 */
	boolean addRooms(int id, String location, int numRooms, int price);

	/**
	 * Deletes all Rooms from a location. It may not succeed if there are
	 * reservations for this location.
	 *
	 * @return success
	 */
	boolean deleteRooms(int id, String location);

	/**
	 * Returns the number of rooms available at a location
	 */
	int queryRooms(int id, String location);

	/**
	 * Returns the price of a room at a location.
	 */
	int queryRoomsPrice(int id, String location);

	/**
	 * Reserves a room certain at this location.
	 */
	boolean reserveRoom(int id, int customer, String locationd);

}
