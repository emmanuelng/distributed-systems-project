package hotels.impl;

import common.data.RMResource;
import common.reservations.ReservableItem;

public class Hotel extends ReservableItem {

	public Hotel(String location, int count, int price) {
		super(location, count, price);
	}

	@Override
	public String toString() {
		return "Hotel(" + getLocation() + ", " + getCount() + ", " + getPrice() + ")";
	}

	@Override
	public RMResource copy() {
		return new Hotel(getLocation(), getCount(), getPrice());
	}

}
