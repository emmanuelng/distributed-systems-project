package common.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Set;

import common.data.actions.CompositeAction;
import common.data.actions.DataAction;

public class RMHashtable<K, V> {

	private Hashtable<K, V> data;
	private Hashtable<Integer, CompositeAction> actions;

	private String pathname;
	private Hashtable<String, ObjectOutputStream> outputStreams;

	/**
	 * Creates a new {@link RMHashtable}. If the given file exists, initializes
	 * it with the data contained in it. Otherwise creates the file and starts
	 * with no data.
	 * 
	 * @param pathname
	 *            path to the save file
	 */
	@SuppressWarnings("unchecked")
	public RMHashtable(String pathname) {
		this.pathname = pathname;
		this.outputStreams = new Hashtable<>();

		try {
			// Recover data from the main record
			FileInputStream fis = new FileInputStream(pathname);
			ObjectInputStream ois = new ObjectInputStream(fis);
			this.data = (Hashtable<K, V>) ois.readObject();
			ois.close();
		} catch (ClassNotFoundException | IOException e) {
			// If for any reason it failed, initialize with empty data
			this.data = new Hashtable<>();
			save();
		}

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
	 * Commits a transactions. After calling this methods, it becomes impossible
	 * to undo the given transaction.
	 */
	public void commit(int id) {
		actions.remove(id);
		save();
	}

	/**
	 * Returns the composite action related to the given transaction. Creates a
	 * new one if it does not exist yet.
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

	/**
	 * Saves the data to disk. If the file does not exist, creates a new one.
	 * 
	 * @return success
	 */
	private boolean save() {
		System.out.println("[RMHashtable] Saving data to disk...");

		ObjectOutputStream mainRecordOut = getOutput(pathname);
		ObjectOutputStream backup1Out = getOutput(pathname + ".backup1");
		ObjectOutputStream backup2Out = getOutput(pathname + ".backup2");

		if (mainRecordOut != null && backup1Out != null && backup2Out != null) {
			try {
				mainRecordOut.writeObject(data);
				backup1Out.writeObject(data);
				backup2Out.writeObject(data);
			} catch (IOException e) {
				System.out.println("[RMHashtable] Error: Unable to write to disk.");
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns the {@link ObjectOutputStream} corresponding to the file located
	 * at the given path.
	 * 
	 * @param pathname
	 *            the path of the file
	 * @return the {@link ObjectOutputStream} or <code>null</code> in case of
	 *         failure
	 */
	private ObjectOutputStream getOutput(String pathname) {
		if (!outputStreams.containsKey(pathname)) {
			try {
				File file = new File(pathname);

				// Create the file if it does not exist.
				// If the file exists, this will have no effect.
				file.getParentFile().mkdirs();
				file.createNewFile();

				FileOutputStream fos = new FileOutputStream(file);
				outputStreams.put(pathname, new ObjectOutputStream(fos));

			} catch (IOException e) {
				System.out.println("[RMHashtable] Error: Unable to open " + pathname);
			}
		}

		return outputStreams.get(pathname);
	}

}
