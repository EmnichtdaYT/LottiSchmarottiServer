package de.emnichtda.lottischmarotti.server.entitys.events.connection;

import de.emnichtda.lottischmarotti.server.entitys.connection.ConnectionHandler;

public interface ConnectEvent {
	public void onConnect(ConnectionHandler connected);
}
