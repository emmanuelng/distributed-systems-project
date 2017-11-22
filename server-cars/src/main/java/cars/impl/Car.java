package cars.impl;

import common.reservations.ReservableItem;

public class Car extends ReservableItem {

	private static final long serialVersionUID = 8368184046106043826L;

	/**
	 * Builds a new {@link Car} object.
	 */
	public Car(String location, int numCars, int price) {
		super(location, numCars, price);
	}

	@Override
	public String toString() {
		return "Car(" + getLocation() + ", " + getCount() + ", " + getPrice() + ")";
	}

}
