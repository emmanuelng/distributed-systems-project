package cars.impl;

import common.data.RMResource;
import common.reservations.ReservableItem;

public class Car extends ReservableItem {

	private static final long serialVersionUID = 8982812673841658486L;

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

	@Override
	public RMResource copy() {
		return new Car(getLocation(), getCount(), getPrice());
	}

}
