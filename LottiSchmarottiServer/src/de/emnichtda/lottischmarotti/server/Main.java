package de.emnichtda.lottischmarotti.server;

import de.emnichtda.lottischmarotti.server.entitys.game.Game;
import de.emnichtda.lottischmarotti.server.entitys.logger.Logger;

public class Main {
	
	private Game game;

	private static Main instance;

	public Main() {

		if (instance == null)
			instance = this;
		else {
			Logger.getInstance().logError("A game instance is already running. Exiting...");
			return;
		}
		
		game = new Game();

	}

	public Game getGame() {
		return game;
	}

	public static void main(String[] args) {
		new Main();
	}

	public static Main getInstance() {
		return instance;
	}
}
