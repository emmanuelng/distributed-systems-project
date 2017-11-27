package common.reservations;

import common.data.RMResource;

public abstract class ReservableItem implements RMResource {

	/**
	 * 
	 */
	private static final long serialVersionUID = 803614182016115952L;
	private int count;
	private int price;
	private int reserved;
	private String location;

	public ReservableItem(String location, int count, int price) {
		this.count = count;
		this.price = price;
		this.reserved = 0;
		this.location = location;
	}

	public void setCount(int id, int c) {
		count = c;
	}

	public void setPrice(int id, int p) {
		price = p;
	}

	public void setReserved(int id, int r) {
		reserved = r;
	}

	public int getCount() {
		return count;
	}

	public int getPrice() {
		return price;
	}

	public int getReserved() {
		return reserved;
	}

	public String getLocation() {
		return location;
	}

	public void abort(int id) {

	}

	public void commit(int id) {

	}

	@Override
	public abstract String toString();

}
