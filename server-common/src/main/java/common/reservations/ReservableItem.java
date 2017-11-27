package common.reservations;

import java.io.Serializable;
import java.util.HashMap;

import common.data.actions.CompositeAction;
import common.data.actions.DataAction;

public abstract class ReservableItem {

	/**
	 * Object encapsulating the state of the reservatble item.
	 * 
	 * @author emmanuel
	 */
	private static class ReservableItemState implements Serializable {

		private static final long serialVersionUID = 5823082988949194184L;

		private int count;
		private int price;
		private int reserved;
		private String location;

		public ReservableItemState(int count, int price, int reserved, String location) {
			this.count = count;
			this.price = price;
			this.reserved = reserved;
			this.location = location;
		}
	}

	/**
	 * Data action for a set count action.
	 */
	private static class SetCountDataAction implements DataAction {

		private static final long serialVersionUID = 8226373465956584117L;
		private ReservableItemState state;
		private int oldCount;

		public SetCountDataAction(ReservableItemState state, int oldCount) {
			this.state = state;
			this.oldCount = oldCount;
		}

		@Override
		public void undo() {
			state.count = oldCount;
		}

	}

	/**
	 * Data action for a set price action.
	 */
	private static class SetPriceDataAction implements DataAction {

		private static final long serialVersionUID = 5226195108279981869L;
		private ReservableItemState state;
		private int oldPrice;

		public SetPriceDataAction(ReservableItemState state, int oldPrice) {
			this.state = state;
			this.oldPrice = oldPrice;
		}

		@Override
		public void undo() {
			state.price = oldPrice;
		}

	}

	/**
	 * Data action for a set reserved action.
	 */
	private static class SetReservedDataAction implements DataAction {

		private static final long serialVersionUID = 6717150813703052540L;
		private ReservableItemState state;
		private int oldReserved;

		public SetReservedDataAction(ReservableItemState state, int oldReserved) {
			this.state = state;
			this.oldReserved = oldReserved;
		}

		@Override
		public void undo() {
			state.reserved = oldReserved;
		}

	}

	private ReservableItemState state;
	private HashMap<Integer, CompositeAction> actions;

	public ReservableItem(String location, int count, int price) {
		this.state = new ReservableItemState(count, price, 0, location);
		this.actions = new HashMap<>();
	}

	public ReservableItem(ReservableItem other) {
		this.state = other.state;
		this.actions = new HashMap<>();
	}

	public void setCount(int id, int c) {
		compositeAction(id).add(new SetCountDataAction(state, state.count));
		state.count = c;
	}

	public void setPrice(int id, int p) {
		compositeAction(id).add(new SetPriceDataAction(state, state.price));
		state.price = p;
	}

	public void setReserved(int id, int r) {
		compositeAction(id).add(new SetReservedDataAction(state, state.reserved));
		state.reserved = r;
	}

	public int getCount() {
		return state.count;
	}

	public int getPrice() {
		return state.price;
	}

	public int getReserved() {
		return state.reserved;
	}

	public String getLocation() {
		return state.location;
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
