package hotels.impl;

import common.reservations.ReservableItem;

public class Hotel extends ReservableItem {

	private static final long serialVersionUID = 2986640900718228998L;

	public Hotel(String location, int count, int price) {
		super(location, count, price);
	}

	@Override
	public String toString() {
		return "Hotel(" + getLocation() + ", " + getCount() + ", " + getPrice() + ")";
	}

}
