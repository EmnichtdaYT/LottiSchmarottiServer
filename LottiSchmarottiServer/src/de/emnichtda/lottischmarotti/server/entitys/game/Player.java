package de.emnichtda.lottischmarotti.server.entitys.game;

import de.emnichtda.lottischmarotti.server.entitys.Client;
import de.emnichtda.lottischmarotti.server.entitys.connection.AwaitedInput;
import de.emnichtda.lottischmarotti.server.entitys.connection.ConnectionHandler;
import de.emnichtda.lottischmarotti.server.entitys.connection.enums.AwaitedInputStatus;
import de.emnichtda.lottischmarotti.server.entitys.connection.enums.InputType;
import de.emnichtda.lottischmarotti.server.entitys.connection.parser.Input;
import de.emnichtda.lottischmarotti.server.entitys.events.connection.InputArrivedEvent;

public class Player extends Client{

	public static final int CHARACTERS_OWNED = 3;
	
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
			owningCharacters[i] = new GameCharacter();
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
	
	@Override
	public String getLogPrefix() {
		return "[Player Client: " + getClientName() + "]" + getConnection().getLogPrefix();
	}

	/***
	 * Tell the player its their turn
	 */
	public void turn() {
		DiceDecisionListener listener = new DiceDecisionListener();
		
		roll(listener);
	}
	
	public void roll(DiceDecisionListener listener) {
		int rolled = Dice.getInstance().roll();
		listener.addRolledNumber(rolled);
		
		getConnection().awaitInput(new AwaitedInput(InputType.ROLL_DECISION, "" + rolled, listener));
	}
	
	public void rollDone() { //Do figure selection etc
		getConnection().getSocket().getGame().finishedTurn(this);
	}

	private class DiceDecisionListener implements InputArrivedEvent {
		public int[] lastRolled = new int[3];
		
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
		public void onArrive(Input response, AwaitedInputStatus status) { //TODO: Check if roll done etc etc etc
			if(!isFull()) {
				roll(this);
			}else {
				rollDone();
			}
		}
		
		public boolean isFull() {
			return lastRolled[lastRolled.length-1] != 0;
		}
	}
	
}
