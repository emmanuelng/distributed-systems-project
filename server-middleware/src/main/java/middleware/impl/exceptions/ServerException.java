package middleware.impl.exceptions;

public class ServerException extends Exception {

	private static final long serialVersionUID = -5499280634231640189L;

	@Override
	public String getMessage() {
		return "Cannot execute the request.";
	}

}
