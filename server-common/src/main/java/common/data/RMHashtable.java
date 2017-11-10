package common.data;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Set;

import common.data.actions.CompositeAction;
import common.data.actions.DataAction;

public class RMHashtable<K, V> {

	private Hashtable<K, V> data;
	private Hashtable<Integer, CompositeAction> actions;

	public RMHashtable() {
		this.data = new Hashtable<>();
		this.actions = new Hashtable<>();
	}

	public synchronized V put(int id, K key, V value) {
		V oldValue = data.get(key);

		compositeAction(id).add(new DataAction() {
			@Override
			public void undo() {
				if (oldValue != null) {
					data.put(key, oldValue);
				} else {
					data.remove(key);
				}
			}
		});

		return data.put(key, value);
	}

	public synchronized V get(int id, Object key) {
		return data.get(key);
	}

	@SuppressWarnings("unchecked")
	public synchronized V remove(int id, Object key) {
		V oldvalue = data.get(key);

		compositeAction(id).add(new DataAction() {
			@Override
			public void undo() {
				data.put((K) key, oldvalue);
			}
		});

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
			System.out.println("[RMHashtable] Cancelling actions from transaction " + id);
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
	private CompositeAction compositeAction(int id) {
		System.out.println("[RMHashtable] Getting the composite action of transaction " + id);

		if (!actions.containsKey(id)) {
			actions.put(id, new CompositeAction());
		}

		return actions.get(id);
	}

}
