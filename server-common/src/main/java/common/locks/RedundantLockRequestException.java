package common.locks;

/**
 * The transaction requested a lock that it already had.
 */
public class RedundantLockRequestException extends Exception {

	private static final long serialVersionUID = -4915085469555447117L;
	protected int xid = 0;

	public RedundantLockRequestException(int xid, String msg) {
		super(msg);
		this.xid = xid;
	}

	public int getXId() {
		return this.xid;
	}
}
