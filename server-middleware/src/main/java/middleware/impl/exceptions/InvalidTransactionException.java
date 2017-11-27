package middleware.impl.exceptions;

import java.rmi.RemoteException;

public class InvalidTransactionException extends RemoteException {

	private static final long serialVersionUID = -1443608606597153984L;
	private String message;

	public InvalidTransactionException(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
