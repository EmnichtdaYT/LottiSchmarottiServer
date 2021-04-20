package de.emnichtda.lottischmarotti.server.entitys.exceptions.connection.parser;

public class UnknownRequestException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7263472369795190335L;

	public UnknownRequestException() {
		super();
	}

	public UnknownRequestException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UnknownRequestException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnknownRequestException(String message) {
		super(message);
	}

	public UnknownRequestException(Throwable cause) {
		super(cause);
	}
	
	
	
}
