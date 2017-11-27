package middleware.impl.exceptions;

import java.rmi.RemoteException;

public class NotPreparedException extends RemoteException {

	private static final long serialVersionUID = 2996913934140204771L;

	@Override
	public String getMessage() {
		return "The transaction is not prepared";
	}
}
