package common.data;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class RMHashtable<K, V extends RMResource> {

	private Map<K, V> data;
	private Map<Integer, Map<K, V>> snapshots;

	public RMHashtable() {
		this.data = new Hashtable<>();
		this.snapshots = new Hashtable<>();
	}

	@SuppressWarnings("unchecked")
	public RMHashtable(RMHashtable<K, V> other) {
		this();

		for (Entry<K, V> entry : other.data.entrySet()) {
			data.put(entry.getKey(), (V) entry.getValue().copy());
		}
	}

	public synchronized V put(int id, K key, V value) {
		createSnapshot(id);
		return data.put(key, value);
	}

	public synchronized V get(int id, Object key) {
		createSnapshot(id);
		return data.get(key);
	}

	public synchronized V remove(int id, Object key) {
		createSnapshot(id);
		return data.remove(key);
	}

	public synchronized boolean containsKey(int id, Object key) {
		createSnapshot(id);
		return data.containsKey(key);
	}

	public boolean containsValue(int id, V value) {
		createSnapshot(id);
		return data.containsValue(value);
	}

	public synchronized boolean isEmpty(int id) {
		createSnapshot(id);
		return data.isEmpty();
	}

	public Collection<V> values() {
		return data.values();
	}

	public Set<Entry<K, V>> entrySet() {
		return data.entrySet();
	}

	public boolean commit(int id) {
		snapshots.remove(id);
		return true;
	}

	public boolean abort(int id) {
		if (snapshots.containsKey(id)) {
			data = snapshots.remove(id);
			System.out.println(data);
		}

		return false;
	}

	public RMHashtable<K, V> copy() {
		return new RMHashtable<>(this);
	}

	@SuppressWarnings("unchecked")
	private void createSnapshot(int id) {
		if (!snapshots.containsKey(id)) {
			Map<K, V> snapshot = new Hashtable<>();

			for (Entry<K, V> entry : data.entrySet()) {
				snapshot.put(entry.getKey(), (V) entry.getValue().copy());
			}

			snapshots.put(id, snapshot);
		}
	}

}
