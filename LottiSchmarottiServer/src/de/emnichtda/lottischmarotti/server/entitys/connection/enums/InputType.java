package de.emnichtda.lottischmarotti.server.entitys.connection.enums;

/***
 * 0 - ack
 * 1 - 19 = requested
 * 20 - 39 = not requested
 * 40 - 59 = error
 * @author Erik Zoe Satzinger | EmnichtdaYT
 *
 */

public enum InputType {
	ACKNOWLEDGMENT(0), INITIAL_INFORMATION(1), ROLL_DECISION(2), CLIENT_SHUTDOWN(21), CLIENT_TIMEOUT(23), GENERAL_ERROR(40), CONNECTION_TIMEOUT(41);
	
	private int id;
	
	private InputType(int id) {
		this.id = id;
	}

	/***
	 * get the internal id for this input
	 * @return id id
	 */
	public int getId() {
		return id;
	}

	/***
	 * get type by internal id
	 * @param inputTypeId id
	 * @return InputType type
	 */
	public static InputType getById(int inputTypeId) {
		for(InputType type : InputType.values()){
			if(type.getId() == inputTypeId) {
				return type;
			}
		}
		return null;
	}
}
