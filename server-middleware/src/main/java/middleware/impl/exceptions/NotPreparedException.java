package middleware.impl.exceptions;

public class NotPreparedException extends Exception {

	private static final long serialVersionUID = 2996913934140204771L;

	@Override
	public String getMessage() {
		return "The transaction is not prepared";
	}
}
