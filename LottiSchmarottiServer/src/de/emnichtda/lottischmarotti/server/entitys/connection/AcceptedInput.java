package de.emnichtda.lottischmarotti.server.entitys.connection;

import java.util.ArrayList;

import de.emnichtda.lottischmarotti.server.entitys.connection.enums.AwaitedInputStatus;
import de.emnichtda.lottischmarotti.server.entitys.connection.enums.InputType;
import de.emnichtda.lottischmarotti.server.entitys.connection.parser.Input;
import de.emnichtda.lottischmarotti.server.entitys.events.connection.InputArrivedEvent;

public class AcceptedInput {
	private ArrayList<InputType> acceptedInputTypes = new ArrayList<>();
	private InputArrivedEvent onArrive;
	
	private ConnectionHandler connection;
	
	public AcceptedInput(InputType acceptedType, InputArrivedEvent onArrive) {
		setOnArrive(onArrive);
		acceptedInputTypes.add(acceptedType);
	}
	
	public AcceptedInput(ArrayList<InputType> acceptedInputTypes, InputArrivedEvent onArrive) {
		setOnArrive(onArrive);
		this.acceptedInputTypes.addAll(acceptedInputTypes);
	}

	public ArrayList<InputType> getAcceptedInputTypes() {
		return acceptedInputTypes;
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
	
	protected void arrived(Input response, AwaitedInputStatus status) {
		onArrive.onArrive(response, status);
	}
	
}
