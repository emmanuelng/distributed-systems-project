package common.locks;

/**
 * Represents a data object, i.e. an object that can be locked.
 */
public class DataObj extends TrxnObj {

	// The data members inherited are
	// XObj:: protected int xid;
	// TrxnObj:: protected String strData;
	// TrxnObj:: protected int lockType;
	// TrxnObj:: public static final int READ = 0;
	// TrxnObj:: public static final int WRITE = 1;

	private static final long serialVersionUID = 6885900334634617415L;

	DataObj() {
		super();
	}

	DataObj(int xid, String strData, int lockType) {
		super(xid, strData, lockType);
	}

	@Override
	public int hashCode() {
		return strData.hashCode();
	}

	@Override
	public int key() {
		return strData.hashCode();
	}

	@Override
	public Object clone() {
		DataObj d = new DataObj(this.xid, this.strData, this.lockType);
		return d;
	}
}
