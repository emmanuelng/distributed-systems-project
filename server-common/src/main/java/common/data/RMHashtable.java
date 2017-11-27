package common.data;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Set;

import common.data.actions.CompositeAction;
import common.data.actions.DataAction;

public class RMHashtable<K, V> {

	/**
	 * Data action for a put action.
	 */
	private static class PutDataAction<K, V> implements DataAction {

		private static final long serialVersionUID = 5030128570040056648L;
		private Hashtable<K, V> data;
		private K key;
		private V oldValue;

		public PutDataAction(Hashtable<K, V> data, K key, V oldvalue) {
			this.data = data;
			this.key = key;
			this.oldValue = oldvalue;
		}

		@Override
		public void undo() {
			if (oldValue != null) {
				data.put(key, oldValue);
			} else {
				data.remove(key);
			}
		}

	}

	/**
	 * Data action for a delete action.
	 */
	private static class RemoveDataAction<K, V> implements DataAction {

		private static final long serialVersionUID = -2856717954127246949L;
		private Hashtable<K, V> data;
		private K key;
		private V oldValue;

		@SuppressWarnings("unchecked")
		public RemoveDataAction(Hashtable<K, V> data, Object key, V oldvalue) {
			this.data = data;
			this.key = (K) key;
			this.oldValue = oldvalue;
		}

		@Override
		public void undo() {
			data.put(key, oldValue);
		}

	}

	private Hashtable<K, V> data;
	private Hashtable<Integer, CompositeAction> actions;

	/**
	 * Creates a new {@link RMHashtable}. If the given file exists, initializes it
	 * with the data contained in it. Otherwise creates the file and starts with no
	 * data.
	 * 
	 * @param pathname
	 *            path to the save file
	 */
	public RMHashtable() {
		new Hashtable<>();
		this.data = new Hashtable<>();
		this.actions = new Hashtable<>();
	}

	public synchronized V put(int id, K key, V value) {
		compositeAction(id).add(new PutDataAction<K, V>(data, key, data.get(key)));
		return data.put(key, value);
	}

	public synchronized V get(int id, Object key) {
		return data.get(key);
	}

	public synchronized V remove(int id, Object key) {
		compositeAction(id).add(new RemoveDataAction<K, V>(data, key, data.get(key)));
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

	public Set<Integer> activeTransactions() {
		return actions.keySet();
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
	 * Commits a transactions. After calling this methods, it becomes impossible to
	 * undo the given transaction.
	 */
	public boolean commit(int id) {
		actions.remove(id);
		return true;
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
