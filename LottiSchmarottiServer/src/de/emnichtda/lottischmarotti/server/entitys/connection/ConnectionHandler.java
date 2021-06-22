package de.emnichtda.lottischmarotti.server.entitys.connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import de.emnichtda.lottischmarotti.server.Main;
import de.emnichtda.lottischmarotti.server.entitys.Client;
import de.emnichtda.lottischmarotti.server.entitys.InternalTimer;
import de.emnichtda.lottischmarotti.server.entitys.connection.enums.AwaitedInputStatus;
import de.emnichtda.lottischmarotti.server.entitys.connection.enums.InputType;
import de.emnichtda.lottischmarotti.server.entitys.connection.enums.OutputType;
import de.emnichtda.lottischmarotti.server.entitys.connection.enums.RequestMethod;
import de.emnichtda.lottischmarotti.server.entitys.connection.parser.Input;
import de.emnichtda.lottischmarotti.server.entitys.connection.parser.Output;
import de.emnichtda.lottischmarotti.server.entitys.connection.parser.OutputBuilder;
import de.emnichtda.lottischmarotti.server.entitys.events.connection.NewAwaitedInputListener;
import de.emnichtda.lottischmarotti.server.entitys.exceptions.connection.parser.MaliformedInputExcpetion;
import de.emnichtda.lottischmarotti.server.entitys.exceptions.connection.parser.UnknownInputException;
import de.emnichtda.lottischmarotti.server.entitys.exceptions.connection.parser.UnknownRequestException;
import de.emnichtda.lottischmarotti.server.entitys.game.Game;
import de.emnichtda.lottischmarotti.server.entitys.game.Player;
import de.emnichtda.lottischmarotti.server.entitys.logger.LogType;
import de.emnichtda.lottischmarotti.server.entitys.logger.Logable;
import de.emnichtda.lottischmarotti.server.entitys.logger.Logger;

public class ConnectionHandler implements Logable {

	private static int nextConnectionId = 0;

	private int connectionId;

	private Socket connection;
	private DataInputStream input;
	private DataOutputStream output;
	private SocketHandler socket;

	private Client client;

	private ArrayList<AcceptedInput> acceptedInputs = new ArrayList<>();

	private boolean run = true;

	private ArrayList<AwaitedInput> awaitedInputQueue = new ArrayList<>();

	private ArrayList<NewAwaitedInputListener> newAwiatedInputListeners = new ArrayList<>();

	protected ConnectionHandler(Socket connection, SocketHandler socket) throws IOException {
		this.connection = connection;
		this.socket = socket;

		connectionId = nextConnectionId;
		nextConnectionId++;

		input = new DataInputStream(connection.getInputStream());
		output = new DataOutputStream(connection.getOutputStream());
		init();

	}

	/***
	 * initialize & start listening
	 */
	private void init() {
		Logger.getInstance().logInfo("Connected", this);

		initTimer();

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (run && !connection.isClosed() && connection.isBound()) {
					try {
						handleResponse(input.readUTF());
					} catch (IOException e) {
						if (connection.isBound() && !connection.isClosed()) {
							LogType logType = (client != null && client instanceof Player) ? LogType.ERROR
									: LogType.WARNING;
							endConnection(
									"Unable to read message from client. " + e.getMessage()
											+ ", disconnecting the client.",
									logType,
									OutputBuilder.getInstance().buildOutput(OutputType.GENERAL_ERROR,
											"Maliformed response or other error while reading message. "
													+ e.getMessage()));
						}
					}
				}
			}
		});
		t.start();

		initClient();

	}

	/***
	 * init the client
	 */
	private void initClient() {
		awaitInput(new AwaitedInput(InputType.INITIAL_INFORMATION, (response, status) -> {
			if (!status.equals(AwaitedInputStatus.ARRIVED)) {
				if (isRun()) {
					endConnection("Requested initial informations, client responded with: " + status, LogType.WARNING,
							OutputBuilder.getInstance().buildOutput(OutputType.GENERAL_ERROR,
									"Awaited initial information, got: " + status));
				}
				return;
			}
			if (!response.getRequestMethod().equals(RequestMethod.POST)) {
				if (isRun()) {
					endConnection(
							"Requested initial informations, client responded with method: "
									+ response.getRequestMethod() + " instead of " + RequestMethod.POST,
							LogType.WARNING,
							OutputBuilder.getInstance().buildOutput(OutputType.GENERAL_ERROR,
									"Awaited initial information, got request " + response.getRequestMethod()
											+ " instead of " + RequestMethod.POST));
				}
				return;
			}
			if (response.getParsedArguments().length < 2) {
				if (isRun()) {
					endConnection(
							"Requested initial informations, client responded with too short answer. Expected at least 2, got "
									+ response.getParsedArguments().length,
							LogType.WARNING,
							OutputBuilder.getInstance().buildOutput(OutputType.GENERAL_ERROR,
									"Awaited initial information, got too short answer, expected 2, got "
											+ response.getParsedArguments().length));
				}
				return;
			}

			
			if (response.getParsedArguments()[0].equalsIgnoreCase("PLAYER")) {
				if (Main.getInstance().getGame().isStarted()) {
					endConnection("Player tried to connect but game is already running.", LogType.INFO, OutputBuilder
							.getInstance().buildOutput(OutputType.GENERAL_ERROR, "The game is already running."));
					return;
				}
				
				
				if (response.getParsedArguments()[1].contains("'") || response.getParsedArguments()[1].contains("\"")
						|| response.getParsedArguments()[1].toLowerCase().contains("\\")
						|| response.getParsedArguments()[1].toLowerCase().contains("fuck")
						|| response.getParsedArguments()[1].toLowerCase().contains("hitler")
						|| response.getParsedArguments()[1].toLowerCase().contains("jude")
						|| response.getParsedArguments()[1].toLowerCase().contains("pussy")
						|| response.getParsedArguments()[1].toLowerCase().contains("nigg")) {
					endConnection("Bad username.", LogType.INFO, OutputBuilder.getInstance()
							.buildOutput(OutputType.GENERAL_ERROR, "Please change your username."));
					return;
				}
				
				if (socket.getConnectedPlayers().size() >= Game.MAX_PLAYERS) {
					endConnection("Player tried to connect but game is already full.", LogType.INFO,
							OutputBuilder.getInstance().buildOutput(OutputType.GENERAL_ERROR, "The game is full."));
					return;
				}
				for(Player p : socket.getConnectedPlayers()) {
					if(p.getClientName().equalsIgnoreCase(response.getParsedArguments()[1])) {
						endConnection("Username already taken", LogType.INFO, OutputBuilder.getInstance().buildOutput(OutputType.GENERAL_ERROR, "Your username is already taken in this game."));
						return;
					}
				}
				client = new Player(this, response.getParsedArguments()[1]);
			} else {
				client = new Client(this, response.getParsedArguments()[1]);
			}
			client.init();
			Logger.getInstance().logInfo("Got initial information from client, created instance.", client);
		}));
	}

	/***
	 * Registers the InternalTimer listener
	 */
	private void initTimer() {
		InternalTimer.getInstance().registerEventHandler(() -> {
			recheckAwaitQueue();
		});
	}

	/***
	 * handle the response got from the client
	 * 
	 * @param response message
	 */
	private void handleResponse(String response) {
		Input input = null;
		try {
			input = new Input(response);
		} catch (UnknownRequestException | MaliformedInputExcpetion | UnknownInputException e) {
			Logger.getInstance().logWarning("Got maliformed input from client: " + e.getMessage(), this);
		}
		if (awaitedInputQueue.isEmpty()) {
			boolean inputWasListened = false;
			if (input == null) {
				return;
			}
			for (AcceptedInput acceptedInput : acceptedInputs) {
				if (acceptedInput.getAcceptedInputTypes().contains(input.getInputType())) {
					if (input != null) {
						acceptedInput.arrived(input, AwaitedInputStatus.ARRIVED);
					}
					inputWasListened = true;
				}
			}
			if (inputWasListened) {
				return;
			}
			try {
				sendMessage(OutputBuilder.getInstance().buildOutput(OutputType.GENERAL_ERROR, "Unwanted information"));
			} catch (IOException e) {
				endConnection(
						"Unable to send information message about unwanted information to the client." + e.getMessage()
								+ ", disconnecting the client.",
						LogType.ERROR,
						OutputBuilder.getInstance().buildOutput(OutputType.GENERAL_ERROR,
								"Error while trying to send a message, actually if you are able to see this message a miracle happened. Whatever. "
										+ e.getMessage()));
			}
			return;
		}
		AwaitedInput currentAwaitedInput = awaitedInputQueue.get(0);
		if (!currentAwaitedInput.getStatus().equals(AwaitedInputStatus.WAITING)) {
			Logger.getInstance()
					.logWarning("A awaited input was not waiting anymore but still in the queue. Skipping it.");
			currentAwaitedInput.arrived(null, AwaitedInputStatus.CONNECTION_ERROR);
			awaitedInputQueue.remove(0);
			handleResponse(response);
			return;
		}

		if (input != null) {
			if (input.getInputType().equals(InputType.CLIENT_TIMEOUT)) {
				currentAwaitedInput.resetTimeWhenDisconnect();
				try {
					sendMessage(OutputBuilder.getInstance().buildOutput(OutputType.ACKNOWLEDGMENT, "Reset await time"));
				} catch (IOException e) {
					Logger.getInstance().logWarning(
							"Unable to inform client about acknowledgment awaited input timout reset", this);
				}
			} else {
				awaitedInputQueue.remove(0);
				currentAwaitedInput.arrived(input, AwaitedInputStatus.ARRIVED);
			}
		} else {
			awaitedInputQueue.remove(0);
			currentAwaitedInput.arrived(null, AwaitedInputStatus.MALIFORMED);
		}
		recheckAwaitQueue();
	}

	/***
	 * That moment when you run out of ideas what to write in the documentation for
	 * your methods. <br>
	 * <br>
	 * Once upon a time there was a person named Zoe. They are nonbinary and they
	 * love coding. <br>
	 * They got an assignment for school, they should program a simple game. <br>
	 * Zoe thought it was a good idea to code a multi-player game for this
	 * assignment. <br>
	 * Yea it might be a good idea if Zoe had some more free time. <br>
	 * Now Zoe is sitting in front of their programming this game. <br>
	 * Their head is exploding because the complexity of this project is way <br>
	 * more than their small brain can handle. Zoe hates their former self because
	 * <br>
	 * of this stupid decision to code a multi-player game. <br>
	 * Zoe would rather like to lay in bed all day long and bruise their...<br>
	 * ...<br>
	 * their... <br>
	 * ...<br>
	 * t<br>
	 * h<br>
	 * e<br>
	 * i<br>
	 * r<br>
	 * ... ....<br>
	 * This might be too much personal stuff for an api documentation so ill end the
	 * story here. <br>
	 * And they all lived happily ever after. <br>
	 * <br>
	 * Happily? Not sure about that part. <br>
	 * <br>
	 * <br>
	 * 
	 * If you are still interested in what the method does: It does some stuff with
	 * the awaiting input queue. It rechecks if the first element on the queue did
	 * already run out of time to response. If it did, it disconnects. If it isn't,
	 * it checks if the element is already done, if so it sends the next request to
	 * the client
	 * 
	 * 
	 */
	public void recheckAwaitQueue() {
		if (!awaitedInputQueue.isEmpty()) {
			AwaitedInput currentAwaitedInput = awaitedInputQueue.get(0);
			if (currentAwaitedInput.getStatus().equals(AwaitedInputStatus.CONNECTION_UNSET)) {
				try {
					currentAwaitedInput.setStatus(AwaitedInputStatus.WAITING);
					sendFirstAwaitInputQueueMessage();
				} catch (IOException e) {
					endConnection(
							"Unable to send await input message " + currentAwaitedInput.getType()
									+ " to client because " + e.getMessage() + ", disconnecting the client.",
							LogType.ERROR,
							OutputBuilder.getInstance().buildOutput(OutputType.GENERAL_ERROR,
									"Error while trying to send a message, actually if you are able to see this message a miracle happened. Whatever. "
											+ e.getMessage()));
				}
				return;
			}
			if (!currentAwaitedInput.getStatus().equals(AwaitedInputStatus.WAITING)) {
				Logger.getInstance().logWarning(
						"A awaited input was not waiting anymore but still in the queue. Skipping it.", this);
				currentAwaitedInput.arrived(null, AwaitedInputStatus.CONNECTION_ERROR);
				awaitedInputQueue.remove(0);
				return;
			}
			if (System.currentTimeMillis() > currentAwaitedInput.getTimeWhenDisconnect()) {
				endConnection(
						"Client didn't respond for longer than " + AwaitedInput.MAXIMUM_AWAIT_TIME_IN_MS
								+ " ms with response for: '" + currentAwaitedInput.getType() + "', disconnected them.",
						LogType.WARNING, OutputBuilder.getInstance().buildOutput(OutputType.CONNECTION_TIMEOUT,
								"Timed out after " + AwaitedInput.MAXIMUM_AWAIT_TIME_IN_MS + " ms"));
				return;
			}
		}
	}

	/***
	 * End the connection
	 * 
	 * @param logMessage message in the log
	 * @param logType    type of log message
	 * @param lastOutput message sent to the client (reason for disconnect)
	 */
	public void endConnection(String logMessage, LogType logType, Output lastOutput) {
		if (!run) {
			return;
		}
		run = false;
		Logger.getInstance().log(logMessage + " (Closing connection)", logType, this);
		cancelAllAwaitingInputs(AwaitedInputStatus.CONNECTION_ERROR);
		try {
			sendMessage(lastOutput);
		} catch (IOException e) {
			Logger.getInstance().logWarning("Problem while sending connection close message: " + e.getMessage(), this);
		}
		try {
			close();
		} catch (IOException e) {
			Logger.getInstance().logWarning("Problem while closing connection: " + e.getMessage(), this);
		}
	}

	protected void cancelAllAwaitingInputs(AwaitedInputStatus error) {
		while (!awaitedInputQueue.isEmpty()) {
			AwaitedInput input = awaitedInputQueue.get(0);
			awaitedInputQueue.remove(0);
			input.arrived(null, error);
		}
	}

	/***
	 * sends the GET message from the first element on the await input queue
	 * 
	 * @throws IOException on error
	 */
	protected void sendFirstAwaitInputQueueMessage() throws IOException {
		if (!awaitedInputQueue.isEmpty()) {
			AwaitedInput currentAwaitedInput = awaitedInputQueue.get(0);
			sendMessage(OutputBuilder.getInstance().buildAwaitedInput(currentAwaitedInput.getType(),
					currentAwaitedInput.getMessage()));
		}
	}

	/***
	 * sends the message directly
	 * 
	 * @param Output message
	 * @throws IOException on error
	 */
	public void sendMessage(Output message) throws IOException {
		output.writeUTF(message.getMessage());
	}

	/***
	 * Kills the connection
	 * 
	 * @throws IOException on error
	 */
	public void close() throws IOException {
		run = false;
		input.close();
		output.close();
		connection.close();
		socket.getConnectedPlayers().remove(client);
		socket.getConnections().remove(this);
		socket.fireConnectionDisconnectEvent(this);
		client = null;
	}

	/***
	 * request a new input from the client
	 * 
	 * @param input input
	 */
	public void awaitInput(AwaitedInput input) {
		if (!run)
			return;
		input.setConnection(this);
		awaitedInputQueue.add(input);

		newAwiatedInputListeners.forEach((listener) -> {
			new Thread(new Runnable() {
				@Override
				public void run() {
					listener.onNewAwiatedInput(input);
				}
			}).start();
		});

		recheckAwaitQueue();
	}

	public void registerNewAwaitedInputListener(NewAwaitedInputListener listener) {

	}

	/***
	 * gets a copy of the currently awaited inputs
	 * 
	 * @return ArrayList input queue
	 */
	@SuppressWarnings("unchecked") // It is secure.
	public ArrayList<AwaitedInput> getAwaitedInputQueue() {
		return (ArrayList<AwaitedInput>) awaitedInputQueue.clone();
	}

	/***
	 * get the socket the connection is connected to
	 * 
	 * @return SocketHandler socket
	 */
	public SocketHandler getSocket() {
		return socket;
	}

	/***
	 * get if socket is accepting input from client
	 * 
	 * @return run
	 */
	public boolean isRun() {
		return run;
	}

	/***
	 * get the connection id
	 * 
	 * @return id
	 */
	public int getConnectionId() {
		return connectionId;
	}

	@Override
	public String getLogPrefix() {
		return "[ConnectionHandler: " + getConnectionId() + "]" + socket.getLogPrefix();
	}

	/***
	 * Get the corresponding client (player/bot) (can be null if information not
	 * there yet)
	 * 
	 * @return client client
	 */
	public Client getClient() {
		return client;
	}

	/***
	 * add a accepted input
	 * 
	 * @param input input
	 */
	public void addAcceptedInput(AcceptedInput input) {
		input.setConnection(this);
		acceptedInputs.add(input);
	}

	public void removeAcceptedInput(AcceptedInput input) {
		input.setConnection(null);
		acceptedInputs.remove(input);
	}

	/***
	 * Get a copy of the list of the accepted inputs
	 * 
	 * @return acceptedInputs inputs
	 */
	@SuppressWarnings("unchecked") // Its secure
	public ArrayList<AcceptedInput> getAcceptedInputs() {
		return (ArrayList<AcceptedInput>) acceptedInputs.clone();
	}

	/***
	 * Get a copy of the list of new awaited input listeners
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked") // Its secure
	public ArrayList<NewAwaitedInputListener> getNewAwiatedInputListeners() {
		return (ArrayList<NewAwaitedInputListener>) newAwiatedInputListeners.clone();
	}

}
