package customers.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Customer {

	private class Reservation {

		private String manager;
		private String itemId;
		private int amount;
		private int price;

		Reservation(String manager, String itemId, int price) {
			this.amount = 1;
			this.price = price;
		}

		/**
		 * Returns the reservation as an array of String. The format is [manager,
		 * itemId, amount].
		 */
		public String[] toArray() {
			String[] array = { manager, itemId, amount + "" };
			return array;
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
	public synchronized void reserve(int id, String manager, String itemId, int price) {
		String itemKey = manager + "/" + itemId;
		Reservation reservation = reservations.get(itemKey);

		if (reservation == null) {
			// Create a unique key to access the item quickly in the hash table
			System.out.println("[Customer] Added reservation " + manager + ", " + itemId + ", " + price);
			reservations.put(itemKey, new Reservation(manager, itemId, price));
		} else {
			reservation.amount++;
		}

	}

	public synchronized void cancelReservation(int id, String manager, String itemId) {
		String itemKey = manager + "/" + itemId;
		Reservation reservation = reservations.get(itemKey);

		if (reservation != null) {
			if (reservation.amount-- == 0) {
				reservations.remove(itemKey);
			}
		}

	}

	/**
	 * Returns an array containing all the reservations in their array form
	 * 
	 * @see Reservation#toArray()
	 */
	public String[][] getReservations() {
		String[][] array = new String[reservations.size()][3];
		int i = 0;

		for (Reservation r : reservations.values()) {
			array[i] = r.toArray();
			i++;
		}

		return array;
	}

	/**
	 * Print the current bill of the customer.
	 */
	public String printBill() {
		String bill = "Bill for customer " + cid + "\n";

		for (Entry<String, Reservation> entry : reservations.entrySet()) {
			String itemId = entry.getKey();
			Reservation reservation = entry.getValue();
			bill += reservation.amount + " " + itemId + " $" + reservation.price + "\n";
		}

		return bill;
	}

	/**
	 * Deletes all reservations associated to the given item.
	 */
	public void clearReservationsForItem(int id, String itemId) {
		reservations.remove(itemId);
	}

}
