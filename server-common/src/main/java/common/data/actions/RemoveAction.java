package common.data.actions;

import java.util.Hashtable;

public class RemoveAction<K, V> implements DataAction {

	private Hashtable<K, V> hashtable;
	private K key;
	private V oldValue;

	public RemoveAction(Hashtable<K, V> hashtable, K key) {
		assert hashtable != null;

		this.hashtable = hashtable;
		this.key = key;
		this.oldValue = hashtable.get(key);
	}

	@Override
	public void undo() {
		if (oldValue != null) {
			hashtable.put(key, oldValue);
		}
	}

}
