package middleware.impl.exceptions;

public class TimeoutException extends Exception {

	private static final long serialVersionUID = -3903228385016109502L;

	@Override
	public String getMessage() {
		return "Timeout error. The transaction was aborted.";
	}

}
