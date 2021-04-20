package de.emnichtda.lottischmarotti.server.entitys.events.connection;

import de.emnichtda.lottischmarotti.server.entitys.connection.enums.AwaitedInputStatus;
import de.emnichtda.lottischmarotti.server.entitys.connection.parser.Input;

public interface InputArrivedEvent {
	/***
	 * What happens when the input arrived
	 * @param response the answer, can be null if error occurred
	 * @param status the status of the request
	 */
	public void onArrive(Input response, AwaitedInputStatus status);
}
