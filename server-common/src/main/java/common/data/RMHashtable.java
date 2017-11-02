package common.data;

import java.util.Collection;
import java.util.Hashtable;

public class RMHashtable<K, V> {

	Hashtable<K, V> data;

	public RMHashtable() {
		this.data = new Hashtable<>();
	}

	public synchronized V put(int id, K key, V value) {
		return data.put(key, value);
	}

	public synchronized V get(int id, Object key) {
		return data.get(key);
	}

	public synchronized V remove(int id, Object key) {
		return data.remove(key);
	}

	public synchronized boolean containsKey(int id, Object key) {
		return data.containsKey(key);
	}

	public boolean containsValue(int id, Object value) {
		return data.containsValue(value);
	}

	public synchronized boolean isEmpty(int id) {
		return data.isEmpty();
	}

	public Collection<V> values() {
		return data.values();
	}

}
