package common.data;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Set;

import common.data.actions.CompositeAction;
import common.data.actions.PutAction;
import common.data.actions.RemoveAction;

public class RMHashtable<K, V> {

	private Hashtable<K, V> data;
	private Hashtable<Integer, CompositeAction> actions;

	public RMHashtable() {
		this.data = new Hashtable<>();
		this.actions = new Hashtable<>();
	}

	public synchronized V put(int id, K key, V value) {
		PutAction<K, V> putAction = new PutAction<>(data, key);
		getCompositeAction(id).add(putAction);
		return data.put(key, value);
	}

	public synchronized V get(int id, Object key) {
		return data.get(key);
	}

	public synchronized V remove(int id, Object key) {
		@SuppressWarnings("unchecked")
		RemoveAction<K, V> removeAction = new RemoveAction<>(data, (K) key);
		getCompositeAction(id).add(removeAction);
		return data.remove(key);
	}

	public synchronized boolean containsKey(int id, Object key) {
		return data.containsKey(key);
	}

	public boolean containsValue(int id, V value) {
		return data.containsValue(value);
	}

	public synchronized boolean isEmpty(int id) {
		return data.isEmpty();
	}

	public Collection<V> values() {
		return data.values();
	}

	public Set<Entry<K, V>> entrySet() {
		return data.entrySet();
	}

	/**
	 * Undoes all operations of a given transaction.
	 * 
	 * @param id
	 */
	public void cancel(int id) {
		if (actions.containsKey(id)) {
			System.out.println("Cancelling actions from transaction " + id);
			actions.remove(id).undo();
		}
	}

	/**
	 * Returns the composite action related to the given transaction. Creates a new
	 * one if it does not exist yet.
	 * 
	 * @param id
	 *            the transaction identifier
	 * @return the {@link CompositeAction}
	 */
	private CompositeAction getCompositeAction(int id) {
		if (!actions.containsKey(id)) {
			actions.put(id, new CompositeAction());
		}

		return actions.get(id);
	}

}
