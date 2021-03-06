package flights.impl;

import common.data.RMResource;
import common.reservations.ReservableItem;

public class Flight extends ReservableItem {

	private static final long serialVersionUID = -5888955579328227721L;

	public Flight(int flightNum, int flightSeats, int flightPrice) {
		super(Integer.toString(flightNum), flightSeats, flightPrice);
	}

	@Override
	public String toString() {
		return "Flight(" + getLocation() + ", " + getCount() + ", " + getPrice() + ")";
	}

	@Override
	public RMResource copy() {
		return new Flight(Integer.parseInt(getLocation()), getCount(), getPrice());
	}

}
