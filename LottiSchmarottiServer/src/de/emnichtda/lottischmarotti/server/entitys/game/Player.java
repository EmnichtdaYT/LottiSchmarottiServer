package de.emnichtda.lottischmarotti.server.entitys.game;

import java.io.IOException;

import de.emnichtda.lottischmarotti.server.Main;
import de.emnichtda.lottischmarotti.server.entitys.Client;
import de.emnichtda.lottischmarotti.server.entitys.connection.AwaitedInput;
import de.emnichtda.lottischmarotti.server.entitys.connection.ConnectionHandler;
import de.emnichtda.lottischmarotti.server.entitys.connection.enums.AwaitedInputStatus;
import de.emnichtda.lottischmarotti.server.entitys.connection.enums.InputType;
import de.emnichtda.lottischmarotti.server.entitys.connection.enums.OutputType;
import de.emnichtda.lottischmarotti.server.entitys.connection.parser.Input;
import de.emnichtda.lottischmarotti.server.entitys.connection.parser.OutputBuilder;
import de.emnichtda.lottischmarotti.server.entitys.events.connection.InputArrivedEvent;
import de.emnichtda.lottischmarotti.server.entitys.logger.LogType;
import de.emnichtda.lottischmarotti.server.entitys.logger.Logable;
import de.emnichtda.lottischmarotti.server.entitys.logger.Logger;

public class Player extends Client{

	public static final int CHARACTERS_OWNED = 3;
	
	private int id;
	
	private GameCharacter[] owningCharacters = new GameCharacter[CHARACTERS_OWNED];
	
	public Player(ConnectionHandler connection, String clientName) {
		super(connection, clientName);
		initCharacters();
		
	}

	/***
	 * create characters
	 */
	private void initCharacters() {
		for(int i = 0; i < owningCharacters.length; i++) {
			owningCharacters[i] = new GameCharacter(Main.getInstance().getGame(), i);
			owningCharacters[i].setOwner(this);
		}
	}

	/***
	 * get if player owns a character
	 * @param gameCharacter character to check ownership of
	 * @return boolean if owns
	 */
	public boolean isOwningCharacter(GameCharacter gameCharacter) {
		for(GameCharacter character : owningCharacters) {
			if(character == gameCharacter) return true;
		}
		return false;
	}
	
	public GameCharacter[] getOwningCharacters() {
		return owningCharacters;
	}
	
	@Override
	public String getLogPrefix() {
		return "[Player Client: " + getClientName() + "]" + getConnection().getLogPrefix();
	}

	/***
	 * Tell the player its their turn
	 */
	public void turn() {
		DiceDecisionListener listener = new DiceDecisionListener(this);
		
		roll(listener);
	}
	
	public void roll(DiceDecisionListener listener) {
		int rolled = Dice.getInstance().roll();
		listener.addRolledNumber(rolled);
		
		getConnection().awaitInput(new AwaitedInput(InputType.ROLL_DECISION, rolled + " continue? type 'c'", listener));
	}
	
	public void rollDone(int rolled) {
		CharacterSelectionListener listener = new CharacterSelectionListener(this, rolled);
		getConnection().awaitInput(new AwaitedInput(InputType.ROLL_DECISION, "which character you want to select, type number. You rolled '" + rolled + "'", listener));
	}

	private class DiceDecisionListener implements InputArrivedEvent, Logable {
		public int[] lastRolled = new int[3];
		
		private Player player;
		
		public DiceDecisionListener(Player player) {
			this.player = player;
		}
		
		public void addRolledNumber(int rolled) {
			for(int i = 0; i < lastRolled.length; i++) {
				if(lastRolled[i] == 0) {
					lastRolled[i] = rolled;
					return;
				}
			}
			throw new IndexOutOfBoundsException("There are already 3 dice rolls saved.");
		}
		
		@Override
		public void onArrive(Input response, AwaitedInputStatus status) {
			if(!status.equals(AwaitedInputStatus.ARRIVED)) {
				getConnection().endConnection("Player answered with maliformed DiceDecision", LogType.ERROR, OutputBuilder.getInstance().buildOutput(OutputType.GENERAL_ERROR, "Maliformed DiceDecision. Closing Connection"));
				return;
			}
			if(response.getParsedArguments().length!=1) {
				Logger.getInstance().logWarning("got invalid argument length for roll decision", this);
				return;
			}
			if(!isFull() && response.getParsedArguments().length == 1 && response.getParsedArguments()[0].equals("c")) {
				roll(this);
			}else{
				int rolled = 0;
				
				for(int i = 0; i < lastRolled.length; i++) {
					rolled += lastRolled[i];
				}
				
				rollDone(rolled);
			}
		}
		
		public boolean isFull() {
			return lastRolled[lastRolled.length-1] != 0;
		}

		@Override
		public String getLogPrefix() {
			return "[Dice Decision listerner]" + this.getPlayer().getLogPrefix();
		}

		public Player getPlayer() {
			return player;
		}
	}
	
	private class CharacterSelectionListener implements InputArrivedEvent, Logable {

		private Player player;
		private int rolled;
		
		public CharacterSelectionListener(Player player, int rolled) {
			this.player = player;
			this.rolled = rolled;
		}

		@Override
		public String getLogPrefix() {
			return "[Character Decision Listener]" + player.getLogPrefix();
		}

		@Override
		public void onArrive(Input response, AwaitedInputStatus status) {
			if(status!=AwaitedInputStatus.ARRIVED) {
				getConnection().endConnection("Player answered with maliformed DiceDecision", LogType.ERROR, OutputBuilder.getInstance().buildOutput(OutputType.GENERAL_ERROR, "Maliformed CharacterSelection. Closing connection"));
				return;
			}
			
			if(response.getParsedArguments().length != 1) {
				getConnection().endConnection("Player answered with maliformed DiceDecision. Invalid amout of parameters", LogType.ERROR, OutputBuilder.getInstance().buildOutput(OutputType.GENERAL_ERROR, "Maliformed CharacterSelection. Requires one parameter. Closing connection"));
				return;
			}
			
			int parsedChar;
			
			try {
				parsedChar = Integer.parseInt(response.getParsedArguments()[0]);
			} catch(NumberFormatException e) {
				getConnection().endConnection("Player answered with non integer character number", LogType.ERROR, OutputBuilder.getInstance().buildOutput(OutputType.GENERAL_ERROR, "Non integer character number. Closing connection"));
				return;
			}
			
			if(parsedChar > Player.CHARACTERS_OWNED || parsedChar < 0) {
				getConnection().endConnection("Player answered with invalid character number", LogType.ERROR, OutputBuilder.getInstance().buildOutput(OutputType.GENERAL_ERROR, "Invalid character number. Closing connection"));
				return;
			}
			
			getConnection().getSocket().getGame().finishedTurn(player, parsedChar, rolled);
		}
		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;		
	}

	public void sendUpdateCharPositions(String message) {
		try {
			getConnection().sendMessage(OutputBuilder.getInstance().buildOutput(OutputType.CHARACTER_INFO, message));
		} catch (IOException e) {
			Logger.getInstance().logWarning("Problem while sending character info to player", this);
		}
	}
	
}
