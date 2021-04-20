package de.emnichtda.lottischmarotti.server.entitys.connection;

import de.emnichtda.lottischmarotti.server.entitys.connection.enums.AwaitedInputStatus;
import de.emnichtda.lottischmarotti.server.entitys.connection.enums.InputType;
import de.emnichtda.lottischmarotti.server.entitys.connection.parser.Input;
import de.emnichtda.lottischmarotti.server.entitys.events.connection.InputArrivedEvent;

public class AwaitedInput {
	
	public static final int MAXIMUM_AWAIT_TIME_IN_MS = 5000;
	
	private long timeWhenDisconnect = Integer.MAX_VALUE;
	
	private InputArrivedEvent onArrive;
	private InputType type;
	private AwaitedInputStatus status = AwaitedInputStatus.CONNECTION_UNSET;
	
	private ConnectionHandler connection;
	
	public AwaitedInput(InputType type, InputArrivedEvent onArrive) {
		this.type = type;
		this.setOnArrive(onArrive);
	}
	
	/***
	 * get what type of information was/is awaited
	 * @return AwaitedInputType awaited
	 */
	public InputType getType() {
		return type;
	}

	/***
	 * get the system time in ms when the connection will be disconnected if no answer was received at this point
	 * @return long time
	 */
	public long getTimeWhenDisconnect() {
		return timeWhenDisconnect;
	}
	
	/***
	 * set the system time in ms when the connection will be disconnected if no answer was received at this point
	 * @param time
	 */
	protected void setTimeWhenDisconnect(long time) {
		timeWhenDisconnect = time;
	}

	/***
	 * returns the event handler which gets executed when the response arrives
	 * @return action on arrive
	 */
	public InputArrivedEvent getOnArrive() {
		return onArrive;
	}

	/***
	 * set the event handler which gets executed when the response arrives
	 * @param onArrive action on arrive
	 */
	public void setOnArrive(InputArrivedEvent onArrive) {
		this.onArrive = onArrive;
	}

	protected void arrived(Input response, AwaitedInputStatus status) {
		setStatus(status);
		onArrive.onArrive(response, status);
	}

	/***
	 * get the connection from which this input is being awaited
	 * @return ConnectionHandler connection
	 */
	public ConnectionHandler getConnection() {
		return connection;
	}

	/***
	 * Set the connection from which this input is being awaited. INTERNAL ONLY!
	 * @param connection
	 */
	protected void setConnection(ConnectionHandler connection) {
		this.connection = connection;
	}

	/***
	 * Get the current status of this request
	 * @return AwaitedInputStatus status
	 */
	public AwaitedInputStatus getStatus() {
		return status;
	}

	/***
	 * set the current status of this request
	 * @param status
	 */
	protected void setStatus(AwaitedInputStatus status) {
		if(this.status.equals(AwaitedInputStatus.CONNECTION_UNSET) && status.equals(AwaitedInputStatus.WAITING)) {
			setTimeWhenDisconnect(System.currentTimeMillis()+MAXIMUM_AWAIT_TIME_IN_MS);
		}
		this.status = status;
	}
	
}
