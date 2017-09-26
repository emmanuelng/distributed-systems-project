package customers.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Customer {

	private class Reservation {

		private int amount;
		private int price;

		Reservation(int price) {
			this.amount = 1;
			this.price = price;
		}

	};

	private int cid;
	private Map<String, Reservation> reservations;

	/**
	 * Builds a new {@link Customer} with the given id.
	 */
	public Customer(int cid) {
		this.cid = cid;
		this.reservations = new HashMap<>();
	}

	/**
	 * Reserves one instance of the given item.
	 */
	public synchronized void reserve(int id, String itemId, int price) {
		Reservation reservation = reservations.get(itemId);

		if (reservation == null) {
			reservations.put(itemId, new Reservation(price));
		} else {
			reservation.amount++;
		}

	}

	/**
	 * Returns all the reservations of the customer as an array of strings formatted
	 * in the following way: "manager/itemId/amount".
	 */
	public String[] getReservations() {
		String[] array = new String[reservations.size()];
		int i = 0;

		for (Entry<String, Reservation> entry : reservations.entrySet()) {
			array[i] = entry.getKey() + "/" + entry.getValue().amount;
			i++;
		}

		return array;
	}

	public String printBill() {
		String bill = "Bill for customer " + cid + "\n";

		for (Entry<String, Reservation> entry : reservations.entrySet()) {
			String itemId = entry.getKey();
			Reservation reservation = entry.getValue();
			bill += reservation.amount + " " + itemId + " $" + reservation.price + "\n";
		}

		return bill;
	}

}
