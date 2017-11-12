package common.reservations;

import java.util.HashMap;

import common.data.actions.CompositeAction;
import common.data.actions.DataAction;

public abstract class ReservableItem {

	private int count;
	private int price;
	private int reserved;
	private String location;

	private HashMap<Integer, CompositeAction> actions;

	public ReservableItem(String location, int count, int price) {
		this.location = location;
		this.count = count;
		this.price = price;
		this.reserved = 0;

		this.actions = new HashMap<>();
	}

	public void setCount(int id, int c) {
		int oldvalue = count;

		compositeAction(id).add(new DataAction() {
			@Override
			public void undo() {
				count = oldvalue;
			}
		});

		count = c;
	}

	public void setPrice(int id, int p) {
		int oldvalue = price;

		compositeAction(id).add(new DataAction() {
			@Override
			public void undo() {
				price = oldvalue;
			}
		});

		price = p;
	}

	public void setReserved(int id, int r) {
		int oldvalue = r;

		compositeAction(id).add(new DataAction() {
			@Override
			public void undo() {
				reserved = oldvalue;
			}
		});

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

	public void cancel(int id) {
		if (actions.containsKey(id)) {
			actions.remove(id).undo();
		}
	}
	
	public void commit(int id) {
		actions.remove(id);
	}

	@Override
	public abstract String toString();

	private CompositeAction compositeAction(int id) {
		if (!actions.containsKey(id)) {
			actions.put(id, new CompositeAction());
		}

		return actions.get(id);
	}

}
