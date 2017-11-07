package common.data.actions;

import java.util.Hashtable;

public class PutAction<K, V> implements DataAction {

	private Hashtable<K, V> hashtable;
	private K key;
	private V oldValue;

	public PutAction(Hashtable<K, V> data, K key) {
		assert data != null;

		this.hashtable = data;
		this.key = key;
		this.oldValue = data.get(key);
	}

	@Override
	public void undo() {
		if (oldValue == null) {
			hashtable.remove(key);
		} else {
			hashtable.put(key, oldValue);
		}
	}

}
