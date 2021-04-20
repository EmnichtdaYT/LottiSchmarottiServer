package de.emnichtda.lottischmarotti.server.entitys.exceptions.connection.parser;

public class UnknownInputException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4957304489132321338L;

	public UnknownInputException() {
		super();
	}

	public UnknownInputException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UnknownInputException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnknownInputException(String message) {
		super(message);
	}

	public UnknownInputException(Throwable cause) {
		super(cause);
	}
	
	
}
