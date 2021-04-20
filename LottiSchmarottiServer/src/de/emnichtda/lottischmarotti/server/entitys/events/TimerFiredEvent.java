package de.emnichtda.lottischmarotti.server.entitys.events;

public interface TimerFiredEvent {
	/***
	 * What happens when the timer clocks.
	 * Clocks every 500 ms
	 */
	public void onClock();
}
