package middleware.impl.exceptions;

import java.rmi.RemoteException;

public class TimeoutException extends RemoteException {

	private static final long serialVersionUID = -3903228385016109502L;

	@Override
	public String getMessage() {
		return "Timeout error. The transaction was aborted.";
	}

}
