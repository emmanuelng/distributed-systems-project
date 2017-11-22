package flights.impl;

import common.reservations.ReservableItem;

public class Flight extends ReservableItem {

	private static final long serialVersionUID = -9106339364286800746L;

	public Flight(int flightNum, int flightSeats, int flightPrice) {
		super(Integer.toString(flightNum), flightSeats, flightPrice);
	}

	@Override
	public String toString() {
		return "Flight(" + getLocation() + ", " + getCount() + ", " + getPrice() + ")";
	}

}
