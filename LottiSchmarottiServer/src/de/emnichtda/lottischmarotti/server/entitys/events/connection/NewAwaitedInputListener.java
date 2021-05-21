package de.emnichtda.lottischmarotti.server.entitys.events.connection;

import de.emnichtda.lottischmarotti.server.entitys.connection.AwaitedInput;

public interface NewAwaitedInputListener {
	/***
	 * What happens when there is a new awaited input
	 * @param input
	 */
	public void onNewAwiatedInput(AwaitedInput input);
}
