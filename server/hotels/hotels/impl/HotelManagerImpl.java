package hotels.impl;

import common.reservations.ReservationManager;
import hotels.HotelManager;
import network.server.RMIServer;

public class HotelManagerImpl extends ReservationManager<Hotel> implements HotelManager {

	public static void main(String[] args) {
		int port = 1099;

		if (args.length == 1) {
			port = Integer.parseInt(args[0]);
		} else if (args.length != 0 && args.length != 1) {
			System.err.println("Wrong usage");
			System.exit(1);
		}

		RMIServer server = RMIServer.newServer(new HotelManagerImpl(), port);
		server.start();
	}

	public HotelManagerImpl() {
		super();
	}

	@Override
	public boolean addRooms(int id, String location, int numRooms, int price) {
		if (getItem(id, location) == null) {
			addItem(id, location, new Hotel(location, numRooms, price));
		} else {
			increaseItemCount(id, location, numRooms, price);
		}

		return true;
	}

	@Override
	public boolean deleteRooms(int id, String location) {
		return deleteItem(id, location);
	}

	@Override
	public int queryRooms(int id, String location) {
		return queryNum(id, location);
	}

	@Override
	public int queryRoomsPrice(int id, String location) {
		return queryPrice(id, location);
	}

	@Override
	public boolean reserveRoom(int id, String location) {
		return reserveItem(id, location);
	}

	@Override
	public boolean releaseRoom(int id, String location, int amount) {
		return increaseItemCount(id, location, amount, 0);
	}

}
