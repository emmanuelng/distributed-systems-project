package common.reservations;

import common.data.RMHashtable;
import middleware.Middleware;

public abstract class ReservationManager<R extends ReservableItem> {

	private RMHashtable<String, R> reservableItems;

	/**
	 * Builds a new {@link ReservationManager}
	 * 
	 * @param resourceName
	 *            the name of the managed resource. Must be unique.
	 */
	public ReservationManager() {
		this.reservableItems = new RMHashtable<>();
	}

	/**
	 * Adds an item to the list of items. The key must be unique in the manager and
	 * should not contain slashes "/".
	 * 
	 * @see Middleware#deleteCustomer(int, int)
	 * @return success
	 */
	protected boolean addItem(int id, String key, R instance) {
		log("addItem(" + id + ", " + key + ") called");

		if (reservableItems.containsKey(id, key)) {
			log("addItem(" + id + ", " + key + "): the key already exist. The current value will be overwitten.");
		}

		log("addItem(" + id + ", " + key + ") created new " + instance);
		reservableItems.put(id, key, instance);
		return true;
	}

	/**
	 * Increases the number of available items for an already existing item and
	 * updates its price.
	 * 
	 * @param numItems
	 *            the number of items to add
	 * @param price
	 *            the new price or 0 if the price is unchanged
	 * @return success
	 */
	protected boolean increaseItemCount(int id, String key, int numItems, int price) {
		log("increaseItemCount(" + id + ", " + key + ", " + numItems + ") called");
		R item = reservableItems.get(id, key);

		if (item == null) {
			log("increaseItemCount(" + id + ", " + key + ", " + numItems + ") failed: item does not exist");
			return false;
		}

		if (price > 0) {
			item.setPrice(price);
		}

		item.setCount(item.getCount() + numItems);
		log("increaseItemCount(" + id + ", " + key + ", " + numItems + ") succeeded");
		return true;
	}

	/**
	 * Returns an item given its key.
	 * 
	 * @return the item or null if it does not exist
	 */
	protected R getItem(int id, String key) {
		return reservableItems.get(id, key);
	}

	/**
	 * Deletes an item. Fails if the item is reserved by at least one customer or if
	 * it does not exist.
	 * 
	 * @return success
	 */
	protected boolean deleteItem(int id, String key) {
		log("deleteItem(" + id + ", " + key + ") called");
		boolean success = true;
		R item = reservableItems.get(id, key);

		if (item == null) {
			log("deleteItem(" + id + ", " + key + ") failed: item does not exist");
			success = false;
		} else if (item.getCount() > 0) {
			log("deleteItem(" + id + ", " + key + ") failed: cannot delete item because some customers reserved it");
			success = false;
		} else {
			log("deleteItem(\" + id + \", \" + key + \"): successfully removed item " + item);
			reservableItems.remove(id, item);
		}

		return success;
	}

	/**
	 * Returns the amount of available resources.
	 */
	protected int queryNum(int id, String key) {
		log("queryNum(" + id + ", " + key + ") called");
		int num = 0;
		ReservableItem item = reservableItems.get(id, key);

		if (item != null) {
			num = item.getCount();
			log("queryNum(" + id + ", " + key + ") returns count=" + num);
		} else {
			log("queryNum(" + id + ", " + key + "): item does not exist. Returns 0 by default.");
		}

		return num;
	}

	/**
	 * Returns the price of an item.
	 */
	protected int queryPrice(int id, String key) {
		log("queryPrice(" + id + ", " + key + ") called");
		int price = 0;
		ReservableItem item = reservableItems.get(id, key);

		if (item != null) {
			price = item.getPrice();
			log("queryPrice(" + id + ", " + key + ") returns cost=$" + price);
		} else {
			log("queryPrice(" + id + ", " + key + "): item does not exist. Returns 0 by default.");
		}

		return price;
	}

	/**
	 * Reserves an item by decreasing the amount of available resources.
	 * 
	 * @see ReservationManager#queryNum(int, String)
	 * @return a unique identifier (on this server, not globally) or
	 *         <code>null</code> on failure.
	 */
	protected String reserveItem(int id, String key) {
		log("reserveItem(" + id + ", " + key + ") called");

		String itemId = null;
		R item = reservableItems.get(id, key);

		if (item == null) {
			log("reserveItem(" + id + ", " + key + ") failed: the requested item does not exist");
		} else if (item.getCount() == 0) {
			log("reserveItem(" + id + ", " + key + ") failed: no more items");
		} else {
			item.setCount(item.getCount() - 1);
			item.setReserved(item.getReserved() + 1);
			itemId = key;
			log("reserveItem(" + id + ", " + key + ") succeeded");
		}

		return itemId;
	}

	/**
	 * Writes a message to the log.
	 */
	private void log(String message) {
		System.out.println("[ReservationManager] " + message);
	}

}
