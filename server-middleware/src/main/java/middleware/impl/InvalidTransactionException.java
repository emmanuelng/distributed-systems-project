package middleware.impl;

public class InvalidTransactionException extends Exception {

	private static final long serialVersionUID = -1443608606597153984L;

	@Override
	public String getMessage() {
		return "The given id is invalid. Please use the start command to get a valid id.";
	}
}
