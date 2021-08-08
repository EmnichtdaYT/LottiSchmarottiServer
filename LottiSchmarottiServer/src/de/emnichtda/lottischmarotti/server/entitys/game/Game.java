package de.emnichtda.lottischmarotti.server.entitys.game;

import java.io.IOException;

import de.emnichtda.lottischmarotti.server.entitys.InternalTimer;
import de.emnichtda.lottischmarotti.server.entitys.connection.SocketHandler;
import de.emnichtda.lottischmarotti.server.entitys.logger.Logger;

public class Game {

	public static int MAX_PLAYERS = 3;
	public static int REQUIRED_PLAYERS = 2;

	private long systemTimeWhenStart = -1;
	private boolean isStarted = false;
	
	private BoardHandler board = new BoardHandler();
	private SocketHandler socket;
	
	private Player currentTurn = null;
	private int currentTurnsIndex = -1;
	
	public Game() {
		try {
			socket = new SocketHandler(this);
		} catch (IOException e) {
			Logger.getInstance().logError("Couldn't start the Socket! Stopping the Game...");
			e.printStackTrace();
			stop();
		}
		
		initListenersForGameStart();
	}
	
	private void initListenersForGameStart() {
		socket.registerClientPairedEventHandler((client) -> {
			if(isStarted) {
				return;
			}
			if (socket.getConnectedPlayers().size() == MAX_PLAYERS) {
				startGame();
			}
			if (socket.getConnectedPlayers().size() >= REQUIRED_PLAYERS) {
				if (systemTimeWhenStart < 0) {
					systemTimeWhenStart = System.currentTimeMillis() + 30000;
					Logger.getInstance().logInfo("30 seconds until game start");
				}
			}
		});
		socket.registerConnectionDisconnectListener((connection) -> {
			if(isStarted) {
				return;
			}
			if(socket.getConnectedPlayers().size() < REQUIRED_PLAYERS) {
				if(systemTimeWhenStart > 0) {
					systemTimeWhenStart = -1;
					Logger.getInstance().logInfo("Game start canceled");
				}
			}
		});
		InternalTimer.getInstance().registerEventHandler(() -> {
			if(isStarted) {
				return;
			}
			if (systemTimeWhenStart > 0 && systemTimeWhenStart <= System.currentTimeMillis())
				startGame();
		});
	}
	

	private void startGame() {
		systemTimeWhenStart = Long.MAX_VALUE;
		isStarted = true;

		Logger.getInstance().logInfo("Game is starting.");
		
		currentTurn = socket.getConnectedPlayers().get(0);
		currentTurnsIndex = 0;
		
		currentTurn.turn();
	} 
	
	public void finishedTurn(Player player, int charNumber, int rolled) {
		if(currentTurn != player) {
			Logger.getInstance().logWarning("Player instance reported finished turn but its not their turn", player);
			return;
		}
		
		if(player.getOwningCharacters()[charNumber].canDoStep(rolled)) {		
			player.getOwningCharacters()[charNumber].doStep(rolled);
			nextPlayersTurn();
		} else {
			player.requestAnotherCharacterDecision(rolled);
		}
	}
	
	public void nextPlayersTurn() {
		try {
			Thread.sleep(200); //200 ms time for the clients to change char positions (
		} catch (InterruptedException e) {	}
		currentTurnsIndex++;
		if(currentTurnsIndex>=socket.getConnectedPlayers().size()) {
			currentTurnsIndex = 0;
		}
		currentTurn = socket.getConnectedPlayers().get(currentTurnsIndex);
		
		currentTurn.turn();
	}

	/***
	 * Waits for the user to press a button and then KILLS the server
	 */
	public void pressEnterToKill() {
		System.out.println("Press enter to exit...");
		try {
			System.in.read();
		} catch (IOException e) {
			System.out.println("Press enter to exit failed. Lol. Just lol. Exiting.");
		}
		System.exit(1);
	}

	public void stop() {
		if (socket != null)
			socket.stop();
		pressEnterToKill();
	}

	/***
	 * Get the board
	 * 
	 * @return BoardHandler board
	 */
	public BoardHandler getBoard() {
		return board;
	}

	/**
	 * Get the socket handler
	 * 
	 * @return SocketHandler sockethandler
	 */
	public SocketHandler getSocket() {
		return socket;
	}
	

	public boolean isStarted() {
		return isStarted;
	}
	
	public Player getCurrentTurn() {
		return currentTurn;
	}

	public int getCurrentTurnsIndex() {
		return currentTurnsIndex;
	}

	public void updateCharPositions() {
		String message = "";
		Field[] fields = board.getBoard();
		for(int i = 1; i<board.getBoard().length; i++) {
			message += i-1 + ": " + fields[i].toString() + "; ";
		}
		for(Player player : getSocket().getConnectedPlayers()) {
			player.sendUpdateCharPositions(message);
		}
	}
}
