package common.locks;

import java.io.Serializable;

public class XObj implements Serializable {

	private static final long serialVersionUID = -2583997600828273125L;
	protected int xid = 0;

	XObj() {
		super();
		this.xid = 0;
	}

	XObj(int xid) {
		super();

		if (xid > 0) {
			this.xid = xid;
		} else {
			this.xid = 0;
		}
	}

	@Override
	public String toString() {
		String outString = new String(this.getClass() + "::xid(" + this.xid + ")");
		return outString;
	}

	public int getXId() {
		return this.xid;
	}

	@Override
	public int hashCode() {
		return this.xid;
	}

	@Override
	public boolean equals(Object xobj) {
		if (xobj == null)
			return false;

		if (xobj instanceof XObj) {
			if (this.xid == ((XObj) xobj).getXId()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object clone() {
		try {
			XObj xobj = (XObj) super.clone();
			xobj.setXId(this.xid);
			return xobj;
		} catch (CloneNotSupportedException clonenotsupported) {
			return null;
		}
	}

	/**
	 * Returns the transaction id of the object.
	 */
	public int key() {
		return this.xid;
	}

	// Used by clone.
	public void setXId(int xid) {
		if (xid > 0) {
			this.xid = xid;
		}
		return;
	}
}
