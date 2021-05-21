package de.emnichtda.lottischmarotti.server.entitys.connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import de.emnichtda.lottischmarotti.server.entitys.Client;
import de.emnichtda.lottischmarotti.server.entitys.connection.enums.OutputType;
import de.emnichtda.lottischmarotti.server.entitys.connection.parser.OutputBuilder;
import de.emnichtda.lottischmarotti.server.entitys.events.connection.ConnectEvent;
import de.emnichtda.lottischmarotti.server.entitys.events.connection.ConnectionDisconnectEvent;
import de.emnichtda.lottischmarotti.server.entitys.game.Game;
import de.emnichtda.lottischmarotti.server.entitys.game.Player;
import de.emnichtda.lottischmarotti.server.entitys.events.connection.ClientPairedEvent;
import de.emnichtda.lottischmarotti.server.entitys.logger.LogType;
import de.emnichtda.lottischmarotti.server.entitys.logger.Logable;
import de.emnichtda.lottischmarotti.server.entitys.logger.Logger;

public class SocketHandler implements Logable {

	private Game game;

	private int port;
	private ServerSocket socket;
	private ArrayList<ConnectionHandler> conHandlers;

	private ArrayList<ConnectEvent> connectListeners = new ArrayList<>();
	private ArrayList<ClientPairedEvent> clientPairedListeners = new ArrayList<>();
	private ArrayList<ConnectionDisconnectEvent> connectionDisconnectListeners = new ArrayList<>();

	private ArrayList<Player> connectedPlayers = new ArrayList<>();

	private boolean run = true;

	/***
	 * Start a socket on this port
	 * 
	 * @param port port the server runs on
	 * @throws IOException on error
	 */
	public SocketHandler(Game game, int port) throws IOException {
		this.game = game;
		this.port = port;
		init();
	}

	/***
	 * Start a socket on the default port 9101
	 * 
	 * @throws IOException on error
	 */
	public SocketHandler(Game game) throws IOException {
		port = 9101;
		this.game = game;
		init();
	}

	private void init() throws IOException {
		socket = new ServerSocket(port);

		conHandlers = new ArrayList<>();

		SocketHandler thisInstance = this;

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (run && socket.isBound() && !socket.isClosed()) {
					try {
						ConnectionHandler conHandler = new ConnectionHandler(socket.accept(), thisInstance);
						conHandlers.add(conHandler);
						fireOnConnect(conHandler);
					} catch (IOException e) {
						if (!socket.isClosed()) {
							Logger.getInstance().logWarning("Problem with incoming connection: " + e.getMessage(),
									thisInstance);
						}
					}
				}
			}
		});

		t.start();

		Logger.getInstance().logInfo("Socket started", this);
	}

	/***
	 * send an update of the list with connected clients to all clients
	 */
	public void sendConnectedClientsUpdate() {
		String players = "Players: ";
		String clients = "Clients: ";

		for (ConnectionHandler connection : getConnections()) {
			Client client = connection.getClient();
			if (client != null) {
				if (client instanceof Player)
					players += "'" + client.getClientName() + "' ";
				else
					clients += "'" + client.getClientName() + "' ";
			}
		}

		for (ConnectionHandler connection : conHandlers) {
			if (connection.getClient() != null)
				try {
					connection.sendMessage(OutputBuilder.getInstance().buildOutput(OutputType.CONNECTED_CLIENT_INFO,
							players + clients));
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

	}

	/***
	 * fire the connect event
	 * 
	 * @param connection the new connection
	 */
	private void fireOnConnect(ConnectionHandler connection) {
		connectListeners.forEach((listener) -> {
			new Thread(new Runnable() {
				@Override
				public void run() {
					listener.onConnect(connection);
				}
			}).start();
		});
	}

	/***
	 * Close all connections and the socket
	 */
	public void stop() {
		run = false;
		closeAllConnections();
		closeSocket();
	}

	/***
	 * Kills all the connections
	 */
	private void closeAllConnections() {
		while (!conHandlers.isEmpty()) {
			ConnectionHandler connection = conHandlers.get(0);
			connection.endConnection("Closing a connection", LogType.INFO,
					OutputBuilder.getInstance().buildOutput(OutputType.SERVER_SHUTDOWN, "Server Socket closing"));
			if (conHandlers.contains(connection)) {
				conHandlers.remove(connection);
			}
		}
	}

	/***
	 * Kills the socket
	 */
	private void closeSocket() {
		try {
			socket.close();
		} catch (IOException e) {
			Logger.getInstance().logWarning("Problem while closing socket: " + e.getMessage(), this);
		}
	}

	/***
	 * get a copy of the list of connect listeners
	 * 
	 * @return ArrayList connectListeners
	 */
	@SuppressWarnings("unchecked") // Its secure
	public ArrayList<ConnectEvent> getConnectListeners() {
		return (ArrayList<ConnectEvent>) connectListeners.clone();
	}

	/***
	 * register a listener for new connections
	 * 
	 * @param listener listener
	 */
	public void registerConnectEventHandler(ConnectEvent listener) {
		connectListeners.add(listener);
	}

	/***
	 * get the port the socket is running on
	 * 
	 * @return int port
	 */
	public int getPort() {
		return port;
	}

	@Override
	public String getLogPrefix() {
		return "[Socket port: " + getPort() + "]";
	}

	/***
	 * get the connections on this socket
	 * 
	 * @return connection handlers
	 */
	public ArrayList<ConnectionHandler> getConnections() {
		return conHandlers;
	}

	/***
	 * get the connected players on this socket
	 * 
	 * @return players
	 */
	public ArrayList<Player> getConnectedPlayers() {
		return connectedPlayers;
	}

	/***
	 * get a copy of all client paired event listeners
	 * 
	 * @return client paired listeners
	 */
	@SuppressWarnings("unchecked") // Its secure
	public ArrayList<ClientPairedEvent> getClientPairedListeners() {
		return (ArrayList<ClientPairedEvent>) clientPairedListeners.clone();
	}

	/***
	 * register a listener for new connected&paired clients
	 * 
	 * @param listener
	 */
	public void registerClientPairedEventHandler(ClientPairedEvent listener) {
		clientPairedListeners.add(listener);
	}

	public void fireClientPairedEvent(Client client) {
		if (client instanceof Player) {
			connectedPlayers.add((Player) client);
		}
		sendConnectedClientsUpdate();

		clientPairedListeners.forEach((listener) -> {
			new Thread(new Runnable() {
				@Override
				public void run() {
					listener.onPaired(client);
				}
			}).start();
		});
	}

	/***
	 * register a listener for disconnects
	 * 
	 * @param listener
	 */
	public void registerConnectionDisconnectListener(ConnectionDisconnectEvent listener) {
		connectionDisconnectListeners.add(listener);
	}

	/***
	 * get a copy of all connection disconnect listeners
	 * 
	 * @return connection disconnect listeners
	 */
	public ArrayList<ConnectionDisconnectEvent> getConnectionDisconnectListeners() {
		return connectionDisconnectListeners;
	}

	/***
	 * ConnectionHandler calls this method when the connection closes
	 * 
	 * @param connectionHandler
	 */
	protected void fireConnectionDisconnectEvent(ConnectionHandler connectionHandler) {
		sendConnectedClientsUpdate();

		for (ConnectionDisconnectEvent listener : connectionDisconnectListeners) {
			listener.onConnectionDisconnect(connectionHandler);
		}
	}

	public Game getGame() {
		return game;
	}
}
