package de.emnichtda.lottischmarotti.server.entitys;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import de.emnichtda.lottischmarotti.server.entitys.events.TimerFiredEvent;

/***
 * An internal timer clocking every 500 ms used for routine checks
 * 
 * @author Erik Zoe Satzinger | EmnichtdaYT
 *
 */
public class InternalTimer extends TimerTask {

	private ArrayList<TimerFiredEvent> eventHandlers = new ArrayList<>();
	private static InternalTimer instance;

	/***
	 * get an instance
	 * @return InternalTimer instance
	 */
	public static InternalTimer getInstance() {
		if (instance == null) {
			instance = new InternalTimer();
		}
		return instance;
	}

	private InternalTimer() {
		Timer t = new Timer();
		t.scheduleAtFixedRate(this, 0, 500);
	}

	@Override
	public void run() {

		eventHandlers.forEach((handler) -> {
			new Thread(new Runnable() {
				@Override
				public void run() {
					handler.onClock();
				}
			}).start();
		});

	}

	/***
	 * get all the event handlers which are listening
	 * @return ArrayList eventHandlers
	 */
	public ArrayList<TimerFiredEvent> getEventHandlers() {
		return eventHandlers;
	}

	/***
	 * register a listener when the timer fires. fires every 500 ms
	 * @param eventHandler handler
	 */
	public void registerEventHandler(TimerFiredEvent eventHandler) {
		eventHandlers.add(eventHandler);
	}
}
