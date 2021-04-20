package de.emnichtda.lottischmarotti.server.entitys.logger;

import java.time.LocalDateTime;

public class Logger {
	
	private Logger() {}
	
	private static Logger instance;
	
	/***
	 * gets a logger instance
	 * @return Logger instance
	 */
	public static Logger getInstance() {
		if(instance==null) {
			instance = new Logger();
		}
		return instance;
	}
	
	/***
	 * Logs a warning
	 * @param message message
	 */
	public void logWarning(String message) {
		log(message, LogType.WARNING);
	}
	
	/***
	 * Logs an info
	 * @param message message
	 */
	public void logInfo(String message) {
		log(message, LogType.INFO);
	}
	
	/***
	 * Logs an error
	 * @param message message
	 */
	public void logError(String message) {
		log(message, LogType.ERROR);
	}
	
	/***
	 * Logs a message with a given type
	 * @param message message
	 * @param type type
	 */
	public void log(String message, LogType type) {
		LocalDateTime time = LocalDateTime.now();
		System.out.println("[LOGGER - " + time.getHour() + ":" + time.getMinute() + ":" + time.getSecond() + " - " + type.getPrefix() + "] " + message);
	}
	
	/***
	 * Logs a warning
	 * @param message message
	 * @param instance logable instance which this log message corresponds to
	 */
	public void logWarning(String message, Logable instance) {
		log(instance.getLogPrefix() + " " + message, LogType.WARNING);
	}
	
	/***
	 * Logs an info
	 * @param message message
	 * @param instance logable instance which this log message corresponds to
	 */
	public void logInfo(String message, Logable instance) {
		log(instance.getLogPrefix() + " " + message, LogType.INFO);
	}
	
	/***
	 * Logs an error
	 * @param message message
	 * @param instance logable instance which this log message corresponds to
	 */
	public void logError(String message, Logable instance) {
		log(instance.getLogPrefix() + " " + message, LogType.ERROR);
	}
	
	/***
	 * Logs a message with a given type
	 * @param message message
	 * @param type type
	 * @param instance logable instance which this log message corresponds to
	 */
	public void log(String message, LogType type, Logable instance) {
		log(instance.getLogPrefix() + " " + message, type);
	}
}
