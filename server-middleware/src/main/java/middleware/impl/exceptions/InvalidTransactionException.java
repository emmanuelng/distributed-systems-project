package middleware.impl.exceptions;

public class InvalidTransactionException extends Exception {

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
