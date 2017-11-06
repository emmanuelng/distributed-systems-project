package common.locks;

import java.util.Vector;

/**
 * HashTable class for the Lock Manager.
 */
public class TPHashTable {

	private static final int HASH_DEPTH = 8;

	private Vector<Vector<XObj>> vect;
	private int iSize; // size of the hash table

	TPHashTable(int iSize) {
		this.iSize = iSize;

		vect = new Vector<Vector<XObj>>(iSize);
		for (int i = 0; i < iSize; i++) {
			this.vect.addElement(new Vector<XObj>(TPHashTable.HASH_DEPTH));
		}
	}

	public int getSize() {
		return iSize;
	}

	public synchronized void add(XObj xobj) {
		if (xobj == null)
			return;

		Vector<XObj> vectSlot;

		int hashSlot = (xobj.hashCode() % this.iSize);
		if (hashSlot < 0) {
			hashSlot = -hashSlot;
		}

		vectSlot = vect.elementAt(hashSlot);
		vectSlot.addElement(xobj);
	}

	/**
	 * Returns all the elements in the table that have the same key as the given
	 * XObj.
	 * 
	 * @see XObj#key()
	 */
	public synchronized Vector<XObj> elements(XObj xobj) {
		if (xobj == null)
			return (new Vector<XObj>());

		Vector<XObj> vectSlot; // hash slot
		Vector<XObj> elemVect = new Vector<XObj>(24); // return object

		int hashSlot = (xobj.hashCode() % this.iSize);
		if (hashSlot < 0) {
			hashSlot = -hashSlot;
		}

		vectSlot = vect.elementAt(hashSlot);

		int size = vectSlot.size();
		XObj tmpXobj;
		for (int i = (size - 1); i >= 0; i--) {
			tmpXobj = vectSlot.elementAt(i);
			if (xobj.key() == tmpXobj.key()) {
				elemVect.addElement(tmpXobj);
			}
		}

		return elemVect;
	}

	public synchronized boolean contains(XObj xobj) {
		if (xobj == null)
			return false;

		Vector<XObj> vectSlot;

		int hashSlot = (xobj.hashCode() % this.iSize);
		if (hashSlot < 0) {
			hashSlot = -hashSlot;
		}

		vectSlot = vect.elementAt(hashSlot);
		return vectSlot.contains(xobj);
	}

	public synchronized boolean remove(XObj xobj) {
		if (xobj == null)
			return false;

		Vector<XObj> vectSlot;

		int hashSlot = (xobj.hashCode() % this.iSize);
		if (hashSlot < 0) {
			hashSlot = -hashSlot;
		}

		vectSlot = vect.elementAt(hashSlot);
		return vectSlot.removeElement(xobj);
	}

	public synchronized XObj get(XObj xobj) {
		if (xobj == null)
			return null;

		Vector<XObj> vectSlot;

		int hashSlot = (xobj.hashCode() % this.iSize);
		if (hashSlot < 0) {
			hashSlot = -hashSlot;
		}

		vectSlot = vect.elementAt(hashSlot);

		XObj xobj2;
		int size = vectSlot.size();
		for (int i = 0; i < size; i++) {
			xobj2 = vectSlot.elementAt(i);
			if (xobj.equals(xobj2)) {
				return xobj2;
			}
		}
		return null;
	}

	public Vector<XObj> allElements() {
		Vector<XObj> vectSlot = null;
		XObj xobj = null;
		Vector<XObj> hashContents = new Vector<XObj>(1024);

		for (int i = 0; i < this.iSize; i++) { // walk down hashslots
			if ((this.vect).size() > 0) { // contains elements?
				vectSlot = (this.vect).elementAt(i);

				for (int j = 0; j < vectSlot.size(); j++) { // walk down single hash slot, adding elements.
					xobj = vectSlot.elementAt(j);
					hashContents.addElement(xobj);
				}
			}
			// else contributes nothing.
		}

		return hashContents;
	}

	public synchronized void removeAll(XObj xobj) {
		if (xobj == null)
			return;

		Vector<XObj> vectSlot;

		int hashSlot = (xobj.hashCode() % this.iSize);
		if (hashSlot < 0) {
			hashSlot = -hashSlot;
		}

		vectSlot = vect.elementAt(hashSlot);

		XObj xobj2;
		int size = vectSlot.size();
		for (int i = (size - 1); i >= 0; i--) {
			xobj2 = vectSlot.elementAt(i);
			if (xobj.key() == xobj2.key()) {
				vectSlot.removeElementAt(i);
			}
		}
	}
}
