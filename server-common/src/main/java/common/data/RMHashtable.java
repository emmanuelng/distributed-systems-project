package common.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import common.files.SaveFile;

public class RMHashtable<K, V extends RMResource> implements Serializable {

	private static final long serialVersionUID = -3672016922396332114L;

	private String rm;
	private String name;

	private Map<K, V> data;
	private Map<Integer, Map<K, V>> snapshots;

	private SaveFile<Map<K, V>> dataFile;
	private SaveFile<Map<Integer, Map<K, V>>> snapshotsFile;

	public RMHashtable(String rm, String name) {
		this.rm = rm;
		this.name = name;

		this.data = new Hashtable<>();
		this.snapshots = new Hashtable<>();

		this.dataFile = new SaveFile<>(rm, name + "_data");
		this.snapshotsFile = new SaveFile<>(rm, name + "_snapshots");

		loadSave();
	}

	@SuppressWarnings("unchecked")
	public RMHashtable(RMHashtable<K, V> other) {
		this(other.rm, other.name);

		for (Entry<K, V> entry : other.data.entrySet()) {
			data.put(entry.getKey(), (V) entry.getValue().copy());
		}
	}

	public synchronized V put(int id, K key, V value) {
		createSnapshot(id);
		V result = data.put(key, value);

		saveData();
		return result;
	}

	public synchronized V get(int id, Object key) {
		createSnapshot(id);
		return data.get(key);
	}

	public synchronized V remove(int id, Object key) {
		createSnapshot(id);
		V result = data.remove(key);

		saveData();
		return result;
	}

	public synchronized boolean containsKey(int id, Object key) {
		createSnapshot(id);
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

	public boolean prepare(int id) {
		return snapshots.containsKey(id);
	}

	public boolean commit(int id) {
		snapshots.remove(id);
		return true;
	}

	public boolean abort(int id) {
		if (snapshots.containsKey(id)) {
			data = snapshots.remove(id);
			return true;
		}

		return false;
	}

	/**
	 * Restores the state of the {@link RMHashtable} from the save files. If they do
	 * not exist yet, creates them.
	 */
	public boolean loadSave() {
		try {
			data = dataFile.read();
			snapshots = snapshotsFile.read();
			return true;

		} catch (IOException | ClassNotFoundException e) {
			return saveData() && saveSnapshots();
		}
	}

	@Override
	public String toString() {
		return data.toString();
	}

	/**
	 * Saves the data to disk.
	 */
	private boolean saveData() {
		try {
			dataFile.save(data);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			log("Unable to write to disk");
			return false;
		}
	}

	/**
	 * Saves the snapshots to disk.
	 */
	private boolean saveSnapshots() {
		try {
			log("Saving data to disk...");
			snapshotsFile.save(snapshots);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			log("Unable to write to disk");
			return false;
		}
	}

	/**
	 * Creates a "snapshot" of the {@link RMHashtable}. meaning a deep copy at the
	 * time where this method is called. If the given transaction already have a
	 * snapshot, nothing happens.
	 * 
	 * @param id
	 *            the transaction id
	 */
	@SuppressWarnings("unchecked")
	private void createSnapshot(int id) {
		if (!snapshots.containsKey(id)) {
			Map<K, V> snapshot = new Hashtable<>();

			for (Entry<K, V> entry : data.entrySet()) {
				snapshot.put(entry.getKey(), (V) entry.getValue().copy());
			}

			snapshots.put(id, snapshot);
			saveSnapshots();
		}
	}

	private void log(String message) {
		System.out.println("[RMHashtable] " + message);
	}

}
