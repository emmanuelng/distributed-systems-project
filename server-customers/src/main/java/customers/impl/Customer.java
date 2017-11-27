package customers.impl;

import java.util.Map.Entry;

import common.data.RMHashtable;
import common.data.RMResource;

public class Customer implements RMResource {

	private class Reservation implements RMResource {

		private String manager;
		private String itemId;
		private int amount;
		private int price;

		Reservation(String manager, String itemId, int price) {
			this.manager = manager;
			this.itemId = itemId;
			this.amount = 1;
			this.price = price;
		}

		/**
		 * Returns the reservation as an array of String. The format is [manager,
		 * itemId, amount].
		 */
		public String toString() {
			return manager + "/" + itemId + "/" + amount;
		}

		@Override
		public RMResource copy() {
			Reservation copy = new Reservation(manager, itemId, price);
			copy.amount = amount;
			return copy;
		}

	};

	private int cid;
	private RMHashtable<String, Reservation> reservations;

	/**
	 * Builds a new {@link Customer} with the given id.
	 */
	public Customer(int cid) {
		this.cid = cid;
		this.reservations = new RMHashtable<>();
	}

	/**
	 * Reserves one instance of the given item.
	 */
	public synchronized void reserve(int id, String manager, String itemId, int price) {
		String itemKey = manager + "/" + itemId;
		Reservation reservation = reservations.get(id, itemKey);

		if (reservation == null) {
			// Create a unique key to access the item quickly in the hash table
			System.out.println("[Customer] Added reservation " + manager + ", " + itemId + ", " + price);
			reservations.put(id, itemKey, new Reservation(manager, itemId, price));
		} else {
			reservation.amount++;
		}

	}

	public synchronized void cancelReservation(int id, String manager, String itemId) {
		String itemKey = manager + "/" + itemId;
		Reservation reservation = reservations.get(id, itemKey);

		if (reservation != null) {
			if (reservation.amount-- == 0) {
				reservations.remove(id, itemKey);
			}
		}

	}

	/**
	 * Returns an array containing all the reservations in their array form
	 * 
	 * @see Reservation#toString()
	 */
	public String getReservations() {
		String result = "";

		for (Reservation r : reservations.values()) {
			result += r.toString() + ";";
		}

		return result;
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
		reservations.remove(id, itemId);
	}

	/**
	 * Cancels all actions that were performed in the given transaction.
	 */
	public void cancel(int id) {
		reservations.abort(id);
	}

	@Override
	public RMResource copy() {
		Customer copy = new Customer(cid);
		copy.reservations = reservations.copy();
		return copy;
	}

}
