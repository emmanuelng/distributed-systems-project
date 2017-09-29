package cars.impl;

import cars.CarManager;
import common.reservations.ReservationManager;

public class CarManagerImpl extends ReservationManager<Car> implements CarManager {

	public static void main(String[] args) {
		int port = 1099;

		if (args.length == 1) {
			port = Integer.parseInt(args[0]);
		} else if (args.length != 0 && args.length != 1) {
			System.err.println("Wrong usage");
			System.exit(1);
		}

		CarManagerImpl cm = new CarManagerImpl(port);
		cm.start();
	}

	/**
	 * Builds a new {@link CarManagerImpl}.
	 */
	public CarManagerImpl(int port) {
		super(port);
	}

	@Override
	public boolean addCars(int id, String location, int numCars, int price) {
		if (getItem(id, location) == null) {
			addItem(id, location, new Car(location, numCars, price));
		} else {
			increaseItemCount(id, location, numCars, price);
		}

		return true;
	}

	@Override
	public boolean deleteCars(int id, String location) {
		return deleteItem(id, location);
	}

	@Override
	public int queryCars(int id, String location) {
		return queryNum(id, location);
	}

	@Override
	public int queryCarsPrice(int id, String location) {
		return queryPrice(id, location);
	}

	@Override
	public boolean reserveCar(int id, String location) {
		return reserveItem(id, location);
	}

	@Override
	public boolean releaseCars(int id, String location, int amount) {
		return increaseItemCount(id, location, amount, 0);
	}

}
