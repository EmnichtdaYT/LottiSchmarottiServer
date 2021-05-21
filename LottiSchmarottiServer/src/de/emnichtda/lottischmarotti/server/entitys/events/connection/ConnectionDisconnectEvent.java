package de.emnichtda.lottischmarotti.server.entitys.events.connection;

import de.emnichtda.lottischmarotti.server.entitys.connection.ConnectionHandler;

public interface ConnectionDisconnectEvent {
	public void onConnectionDisconnect(ConnectionHandler connection);
}
