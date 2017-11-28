package common.locks;

import java.util.Date;

/**
 * Object that represents a time stamp associated to a lock request.
 */
public class TimeObj extends XObj {

	private static final long serialVersionUID = 5832920269861495262L;
	private Date date = new Date();

	// The data members inherited are
	// XObj:: private int xid;

	TimeObj() {
		super();
	}

	TimeObj(int xid) {
		super(xid);
	}

	public long getTime() {
		return date.getTime();
	}
}
