package de.emnichtda.lottischmarotti.server.entitys.logger;

public enum LogType {
	
	INFO("INFO"), WARNING("WARNING"), ERROR("ERROR");
	
	private String prefix;
	
	private LogType(String prefix) {
		this.prefix = prefix;
	}
	
	/***
	 * Get the prefix the logger shows for this type of message
	 * @return String prefix
	 */
	public String getPrefix() {
		return prefix;
	}
	
}
