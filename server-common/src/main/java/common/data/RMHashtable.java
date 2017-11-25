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
	
	/**
	 * Creates a new {@link RMHashtable}. If the given file exists, initializes it
	 * with the data contained in it. Otherwise creates the file and starts with no
	 * data.
	 * 
	 * @param pathname
	 *            path to the save file
	 */
	public RMHashtable(String pathname) {
		this.pathname = pathname;
		new Hashtable<>();
		this.data = new Hashtable<>();
		this.actions = new Hashtable<>();

		loadSave();
	}

	public synchronized V put(int id, K key, V value) {
		V oldValue = data.get(key);

		compositeAction(id).add(new DataAction() {

			private static final long serialVersionUID = -8694865005726588307L;

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

			private static final long serialVersionUID = 2602552916806504010L;

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
		return save();
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

	/**
	 * Loads the save file if it exists. If not, creates new files with the
	 * {@link RMHashtable#save()} method.
	 */
	@SuppressWarnings("unchecked")
	private void loadSave() {
		try {
			FileInputStream fis = null;
			ObjectInputStream ois = null;

			// Restore the data
			fis = new FileInputStream(pathname + ".data");
			ois = new ObjectInputStream(fis);
			data.putAll((Hashtable<K, V>) ois.readObject());

			// Restore the actions
			fis = new FileInputStream(pathname + ".actions");
			ois = new ObjectInputStream(fis);
			actions.putAll((Hashtable<Integer, CompositeAction>) ois.readObject());

			ois.close();
		} catch (ClassNotFoundException | IOException e) {
			save();
		}
	}

	/**
	 * Saves the data to disk. If the file does not exist, creates a new one.
	 * 
	 * @return success
	 */
	private boolean save() {
		System.out.println("[RMHashtable] Saving data to disk...");

		try {

			// Save the data
			saveObj(pathname + ".data", data);

			// Save the actions
			saveObj(pathname + ".actions", actions);

		} catch (IOException e) {
			System.out.println("[RMHashtable] Unable to write to disk");
			return false;
		}

		return true;
	}

	private void saveObj(String pathname, Object obj) throws IOException {
		// Open file. Create it if it does not exist
		File file = new File(pathname);
		file.getParentFile().mkdirs();
		file.createNewFile();

		// Write the object to file
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(data);

		oos.close();
	}

}
