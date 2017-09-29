package flights.impl;

import cars.impl.CarManagerImpl;
import common.reservations.ReservationManager;
import flights.FlightManager;
import network.server.RMIServer;

public class FlightManagerImpl extends ReservationManager<Flight> implements FlightManager {

	public static void main(String[] args) {
		int port = 1099;

		if (args.length == 1) {
			port = Integer.parseInt(args[0]);
		} else if (args.length != 0 && args.length != 1) {
			System.err.println("Wrong usage");
			System.exit(1);
		}

		RMIServer server = RMIServer.newServer(new FlightManagerImpl(), port);
		server.start();
	}

	private FlightManagerImpl() {
		super();
	}

	@Override
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice) {
		if (getItem(id, Integer.toString(flightNum)) == null) {
			addItem(id, Integer.toString(flightNum), new Flight(flightNum, flightSeats, flightPrice));
		} else {
			increaseItemCount(id, Integer.toString(flightNum), flightSeats, flightPrice);
		}

		return true;
	}

	@Override
	public boolean deleteFlight(int id, int flightNum) {
		return deleteItem(id, Integer.toString(flightNum));
	}

	@Override
	public int queryFlight(int id, int flightNum) {
		return queryNum(id, Integer.toString(flightNum));
	}

	@Override
	public int queryFlightPrice(int id, int flightNum) {
		return queryPrice(id, Integer.toString(flightNum));
	}

	@Override
	public boolean reserveFlight(int id, int flightNum) {
		return reserveItem(id, Integer.toString(flightNum));
	}

	@Override
	public boolean releaseSeats(int id, int flightNumber, int amount) {
		return increaseItemCount(id, Integer.toString(flightNumber), amount, 0);
	}

}
