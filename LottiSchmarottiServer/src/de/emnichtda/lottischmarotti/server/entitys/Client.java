package de.emnichtda.lottischmarotti.server.entitys;

import de.emnichtda.lottischmarotti.server.entitys.connection.AcceptedInput;
import de.emnichtda.lottischmarotti.server.entitys.connection.ConnectionHandler;
import de.emnichtda.lottischmarotti.server.entitys.connection.enums.InputType;
import de.emnichtda.lottischmarotti.server.entitys.connection.enums.OutputType;
import de.emnichtda.lottischmarotti.server.entitys.connection.parser.OutputBuilder;
import de.emnichtda.lottischmarotti.server.entitys.logger.LogType;
import de.emnichtda.lottischmarotti.server.entitys.logger.Logable;
import de.emnichtda.lottischmarotti.server.entitys.logger.Logger;

public class Client implements Logable{
	private ConnectionHandler connection;
	
	private String clientName;
	
	public Client(ConnectionHandler connection, String clientName) {
		this.connection = connection;
		this.clientName = clientName;
	}

	public void init() {
		connection.addAcceptedInput(new AcceptedInput(InputType.CLIENT_SHUTDOWN, (input, status) -> {
			String reason = "";
			for(int i = 0; i < input.getParsedArguments().length; i++) {
				reason += input.getParsedArguments()[i] + " ";
			}
			connection.endConnection("Disconnect", LogType.INFO, OutputBuilder.getInstance().buildOutput(OutputType.ACKNOWLEDGMENT, "Have a great day"));
			Logger.getInstance().logInfo("Disconnected with reason: " + reason, this);
		}));
		connection.getSocket().fireClientPairedEvent(this);
	}

	public ConnectionHandler getConnection() {
		return connection;
	}

	public String getClientName() {
		return clientName;
	}

	@Override
	public String getLogPrefix() {
		return "[Client: " + getClientName() + "]" + connection.getLogPrefix();
	}
	
}
