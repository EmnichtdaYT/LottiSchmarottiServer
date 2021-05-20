package de.emnichtda.lottischmarotti.server.entitys.connection.enums;

/***
 * 0 - ack
 * 1 - 29 = requested
 * 20 - 39 = not requested
 * 40 - 59 = error
 * @author Erik Zoe Satzinger | EmnichtdaYT
 *
 */
public enum OutputType {
	ACKNOWLEDGMENT(0), INITIAL_INFORMATION(1), SERVER_SHUTDOWN(21), CONNECTED_CLIENT_INFO(22), SERVER_TIMEOUT(23), GENERAL_ERROR(40), CONNECTION_TIMEOUT(41);
	
	private int id;
	
	private OutputType(int id) {
		this.id = id;
	}

	/***
	 * get the internal id for this output
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
	public static OutputType getById(int inputTypeId) {
		for(OutputType type : OutputType.values()){
			if(type.getId() == inputTypeId) {
				return type;
			}
		}
		return null;
	}
	
}
