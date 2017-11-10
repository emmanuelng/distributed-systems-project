package common.locks;

import java.rmi.RemoteException;

/**
 * The transaction is deadlocked. Somebody should abort it.
 */
public class DeadlockException extends RemoteException {

	private static final long serialVersionUID = 3185705368596410669L;
	private int xid = 0;

	public DeadlockException(int xid, String msg) {
		super("The transaction " + xid + " is deadlocked:" + msg);
		this.xid = xid;
	}

	int GetXId() {
		return xid;
	}
}
