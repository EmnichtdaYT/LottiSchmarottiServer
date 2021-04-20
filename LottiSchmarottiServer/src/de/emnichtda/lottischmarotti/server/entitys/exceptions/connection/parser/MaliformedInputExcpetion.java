package de.emnichtda.lottischmarotti.server.entitys.exceptions.connection.parser;

public class MaliformedInputExcpetion extends Exception {

	public MaliformedInputExcpetion(String message) {
		super(message);
	}

	public MaliformedInputExcpetion() {
		super();
	}



	public MaliformedInputExcpetion(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}



	public MaliformedInputExcpetion(String message, Throwable cause) {
		super(message, cause);
	}



	public MaliformedInputExcpetion(Throwable cause) {
		super(cause);
	}



	/**
	 * 
	 */
	private static final long serialVersionUID = 3891241244598579030L;
	
}
