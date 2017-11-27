package common.reservations;

import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import common.data.RMHashtable;
import common.locks.DeadlockException;
import common.locks.LockManager;
import common.locks.TrxnObj;

public abstract class ReservationManager<R extends ReservableItem> {

	private RMHashtable<String, R> reservableItems;
	private LockManager lockManager;

	/**
	 * Builds a new {@link ReservationManager}
	 * 
	 * @param resourceName
	 *            the name of the managed resource. Must be unique.
	 */
	public ReservationManager() {
		this.reservableItems = new RMHashtable<>();
		this.lockManager = new LockManager();
		new HashSet<>();
	}

	/**
	 * Adds an item to the list of items. The key must be unique in the manager.
	 * 
	 * @see Middleware#deleteCustomer(int, int)
	 * @return success
	 * @throws DeadlockException
	 */
	protected boolean addItem(int id, String key, R instance) throws DeadlockException {
		log("addItem(" + id + ", " + key + ") called");

		lockManager.lock(id, "reservableItems", TrxnObj.WRITE);

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
	 * @throws DeadlockException
	 */
	protected boolean increaseItemCount(int id, String key, int numItems, int price) throws DeadlockException {
		log("increaseItemCount(" + id + ", " + key + ", " + numItems + ") called");

		lockManager.lock(id, "reservableItems", TrxnObj.WRITE);
		R item = reservableItems.get(id, key);

		if (item == null) {
			log("increaseItemCount(" + id + ", " + key + ", " + numItems + ") failed: item does not exist");
			return false;
		}

		if (price > 0) {
			item.setPrice(id, price);
		}

		item.setCount(id, item.getCount() + numItems);
		log("increaseItemCount(" + id + ", " + key + ", " + numItems + ") succeeded");

		return true;
	}

	/**
	 * Returns an item given its key.
	 * 
	 * @return the item or null if it does not exist
	 * @throws DeadlockException
	 */
	protected R getItem(int id, String key) throws DeadlockException {
		lockManager.lock(id, "reservableItems", TrxnObj.READ);
		return reservableItems.get(id, key);
	}

	/**
	 * Deletes an item. Fails if the item is reserved by at least one customer or if
	 * it does not exist.
	 * 
	 * @return success
	 * @throws DeadlockException
	 */
	protected boolean deleteItem(int id, String key) throws DeadlockException {
		log("deleteItem(" + id + ", " + key + ") called");

		lockManager.lock(id, "reservableItems", TrxnObj.WRITE);
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
	 * 
	 * @throws DeadlockException
	 */
	protected int queryNum(int id, String key) throws DeadlockException {
		log("queryNum(" + id + ", " + key + ") called");

		lockManager.lock(id, "reservableItems", TrxnObj.READ);
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
	 * 
	 * @throws DeadlockException
	 */
	protected int queryPrice(int id, String key) throws DeadlockException {
		log("queryPrice(" + id + ", " + key + ") called");

		lockManager.lock(id, "reservableItems", TrxnObj.READ);
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
	 * @return success
	 * @throws DeadlockException
	 */
	protected boolean reserveItem(int id, String key) throws DeadlockException {
		log("reserveItem(" + id + ", " + key + ") called");

		lockManager.lock(id, "reservableItems", TrxnObj.READ);
		boolean success = true;
		R item = reservableItems.get(id, key);

		if (item == null) {
			success = false;
			log("reserveItem(" + id + ", " + key + ") failed: the requested item does not exist");
		} else if (item.getCount() == 0) {
			log("reserveItem(" + id + ", " + key + ") failed: no more items");
			success = false;
		} else {
			item.setCount(id, item.getCount() - 1);
			item.setReserved(id, item.getReserved() + 1);
			log("reserveItem(" + id + ", " + key + ") succeeded");
		}

		return success;
	}

	public boolean prepareCommit(int id) {
		// Return true only if the transaction is active, i.e. if it was not committed
		// or aborted yet.
		return reservableItems.activeTransactions().contains(id);
	}

	protected boolean commitTransaction(int id) {
		log("Committing transaction " + id);
		boolean success = true;

		lockManager.unlockAll(id);
		success &= reservableItems.commit(id);

		for (ReservableItem ri : reservableItems.values()) {
			ri.commit(id);
		}

		return success;
	}

	protected boolean abortTransaction(int id) {
		log("Aborting transaction " + id);
		lockManager.unlockAll(id);
		reservableItems.cancel(id);

		for (ReservableItem ri : reservableItems.values()) {
			ri.cancel(id);
		}

		return true;

	}

	protected boolean shutdownManager() {
		return selfDestroyManager(0);
	}

	protected boolean selfDestroyManager(int status) {
		log("Will shut down in 1 second.");

		Timer shutdownTimer = new Timer();
		shutdownTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				System.exit(status);
			}
		}, 1000);

		return true;
	}

	/**
	 * Writes a message to the log.
	 */
	private void log(String message) {
		System.out.println("[ReservationManager] " + message);
	}

}
