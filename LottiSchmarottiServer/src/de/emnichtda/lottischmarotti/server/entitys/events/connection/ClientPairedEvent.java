package de.emnichtda.lottischmarotti.server.entitys.events.connection;

import de.emnichtda.lottischmarotti.server.entitys.Client;

public interface ClientPairedEvent {
	public void onPaired(Client client);
}
