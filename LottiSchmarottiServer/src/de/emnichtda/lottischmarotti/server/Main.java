package de.emnichtda.lottischmarotti.server;

import java.io.IOException;

import de.emnichtda.lottischmarotti.server.entitys.InternalTimer;
import de.emnichtda.lottischmarotti.server.entitys.connection.SocketHandler;
import de.emnichtda.lottischmarotti.server.entitys.game.BoardHandler;
import de.emnichtda.lottischmarotti.server.entitys.logger.Logger;

public class Main {
	
	public static int MAX_PLAYERS = 3;
	public static int REQUIRED_PLAYERS = 2;
	
	private BoardHandler board = new BoardHandler();
	private SocketHandler socket;
	
	private long systemTimeWhenStart = -1;
	private boolean isStarted = false;
	
	private static Main instance;
	
	public Main() {		
		
		if(instance==null) instance = this;
		else {
			Logger.getInstance().logError("A game instance is already running. Exiting...");
			stop();
		}
		
		try {
			socket = new SocketHandler();
		} catch (IOException e) {
			Logger.getInstance().logError("Couldn't start the Socket! Stopping the Server...");
			e.printStackTrace();
			stop();
		}
		
		initClientPairedListener();
		
	}
	
	private void initClientPairedListener() {
		socket.registerClientPairedEventHandler((client) -> {
			if(socket.getConnectedPlayers().size() == MAX_PLAYERS) {
				startGame();
			}
			if(socket.getConnectedPlayers().size() >= REQUIRED_PLAYERS) {
				if(systemTimeWhenStart<0) {
					systemTimeWhenStart = System.currentTimeMillis() + 30000;
					Logger.getInstance().logInfo("30 seconds until game start");
				}
			}
		});
		InternalTimer.getInstance().registerEventHandler(() -> {
			if(systemTimeWhenStart > 0 && systemTimeWhenStart <= System.currentTimeMillis()) startGame();
		});
	}
	
	private void startGame() {
		systemTimeWhenStart = Long.MAX_VALUE;
		isStarted = true;
		Logger.getInstance().logInfo("Game is starting.");
	}

	public void stop() {
		if(socket!=null) socket.stop();
		pressEnterToKill();
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
	
	public static void main(String[] args) {
		new Main();
	}

	/***
	 * Get the board
	 * @return BoardHandler board
	 */
	public BoardHandler getBoard() {
		return board;
	}

	/**
	 * Get the socket handler
	 * @return SocketHandler sockethandler
	 */
	public SocketHandler getSocket() {
		return socket;
	}

	public boolean isStarted() {
		return isStarted;
	}

	public static Main getInstance() {
		return instance;
	}
}
