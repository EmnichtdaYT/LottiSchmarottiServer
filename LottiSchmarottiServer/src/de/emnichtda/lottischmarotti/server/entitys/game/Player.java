package de.emnichtda.lottischmarotti.server.entitys.game;

import de.emnichtda.lottischmarotti.server.entitys.Client;
import de.emnichtda.lottischmarotti.server.entitys.connection.ConnectionHandler;

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

}
