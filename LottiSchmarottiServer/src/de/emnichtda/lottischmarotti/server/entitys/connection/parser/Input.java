package de.emnichtda.lottischmarotti.server.entitys.connection.parser;

import java.util.Arrays;

import de.emnichtda.lottischmarotti.server.entitys.connection.enums.InputType;
import de.emnichtda.lottischmarotti.server.entitys.connection.enums.RequestMethod;
import de.emnichtda.lottischmarotti.server.entitys.exceptions.connection.parser.MaliformedInputExcpetion;
import de.emnichtda.lottischmarotti.server.entitys.exceptions.connection.parser.UnknownInputException;
import de.emnichtda.lottischmarotti.server.entitys.exceptions.connection.parser.UnknownRequestException;

public class Input {

	private String message;
	private String[] parsedArguemnts;
	private RequestMethod requestMethod;
	private InputType inputType;

	/***
	 * Parse a message from the client
	 * 
	 * @param message the message from the client
	 * @throws MaliformedInputExcpetion when input maliformed
	 * @throws UnknownRequestException  when not implemented method
	 * @throws UnknownInputException when not implemented input type
	 */
	public Input(String message) throws UnknownRequestException, MaliformedInputExcpetion, UnknownInputException {
		this.message = message;
		parse();
	}

	/***
	 * Parse the message
	 * @throws MaliformedInputExcpetion when input maliformed
	 * @throws UnknownRequestException when not implemented method
	 * @throws UnknownInputException when not implemented input type
	 */
	private void parse() throws MaliformedInputExcpetion, UnknownRequestException, UnknownInputException {
		String[] splittedMessage = message.split(" ");
		if(splittedMessage.length<2) {
			throw new MaliformedInputExcpetion("Minimum length of 2 not met for message '" + message + "'");
		}
		
		try {
			requestMethod = RequestMethod.valueOf(splittedMessage[0].toUpperCase());
		}catch(IllegalArgumentException e) {
			throw new UnknownRequestException("Type " + splittedMessage[0] + " unknown.");
		}
		
		int inputTypeId;
		try {
			 inputTypeId = Integer.parseInt(splittedMessage[1]);
		}catch(NumberFormatException e) {
			throw new MaliformedInputExcpetion("Input id '" + splittedMessage[1] + "' is not a number");
		}
		inputType = InputType.getById(inputTypeId);
		if(inputType==null) {
			throw new UnknownInputException("Input id '" + inputTypeId + "' is unknown");
		}
		
		parsedArguemnts = new String[splittedMessage.length-2];
		for(int i = 2; i < splittedMessage.length; i++) {
			parsedArguemnts[i-2] = splittedMessage[i];
		}
	}
	
	public String getRawMessage() {
		return message;
	}

	public RequestMethod getRequestMethod() {
		return requestMethod;
	}

	public InputType getInputType() {
		return inputType;
	}

	public String[] getParsedArguments() {
		return parsedArguemnts;
	}

	@Override
	public String toString() {
		return "Input [parsedArguemnts=" + Arrays.toString(parsedArguemnts) + ", requestMethod=" + requestMethod
				+ ", inputType=" + inputType + "]";
	}
}
