package de.emnichtda.lottischmarotti.server.entitys.game;

public class Field {
	private GameCharacter standing;
	private BoardHandler board;
	
	protected Field(BoardHandler parentBoard) {
		this.board = parentBoard;
	}
	
	/***
	 * returns if there is a character standing on the field
	 * @return boolean if populated
	 */
	public boolean isPopulated() {
		return standing != null;
	}

	/***
	 * gets the character currently standing on the field
	 * @return GameCharacter standing on the field
	 */
	public GameCharacter getStanding() {
		return standing;
	}

	/***
	 * sets the character standing on the field
	 * @param standing GameCharacter standing on the field
	 * @throws IllegalStateException when field is already populated
	 */
	public void setStanding(GameCharacter standing) throws IllegalStateException{
		if(isPopulated()) throw new IllegalStateException("The field is already populated.");
		this.standing = standing;
		standing.setField(this);
	}
	
	/***
	 * removes the character which is currently standing on the field from the field
	 * @throws IllegalStateException when the field is not populated
	 */
	public void unsetStanding() throws IllegalStateException{
		if(!isPopulated()) throw new IllegalStateException("The field is not populated.");
		this.standing = null;
		standing.setField(null);
	}

	/***
	 * gets the board the field is on
	 * @return BoardHandler board field is on
	 */
	public BoardHandler getBoard() {
		return board;
	}
	
}
