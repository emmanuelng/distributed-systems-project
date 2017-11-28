package hotels.impl;

import common.data.RMResource;
import common.reservations.ReservableItem;

public class Hotel extends ReservableItem {

	private static final long serialVersionUID = -3083212988694689107L;

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
