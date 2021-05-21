package de.emnichtda.lottischmarotti.server.entitys.connection.parser;

import de.emnichtda.lottischmarotti.server.entitys.connection.enums.InputType;
import de.emnichtda.lottischmarotti.server.entitys.connection.enums.RequestMethod;
import de.emnichtda.lottischmarotti.server.entitys.connection.enums.OutputType;

public class OutputBuilder {
	
	private static OutputBuilder instance;
	
	public static OutputBuilder getInstance() {
		if(instance==null) {
			instance = new OutputBuilder();
		}
		return instance;
	}
	
	private OutputBuilder() { }
	
	public Output buildAwaitedInput(InputType type, String message) { 
		return new Output(RequestMethod.GET + " " + type.getId() + " " + message);
	}
	
	public Output buildOutput(OutputType type, String message) { 
		return new Output(RequestMethod.POST + " " + type.getId() + " " + message);
	}
	
}
