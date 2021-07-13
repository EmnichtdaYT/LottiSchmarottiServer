package de.emnichtda.lottischmarotti.server.entitys.game;

public class BoardHandler {
	
	/***
	 * Defines the amount of fileds. Last field is the win field
	 */
	public static final int FIELDS = 27;
	
	private Field[] board = new Field[FIELDS];
	
	public BoardHandler() {
		initBoard();
	}
	
	/***
	 * fill board with new fields
	 */
	private void initBoard() {
		for(int i = 0; i < board.length; i++) {
			board[i] = new Field(this);
		}
	}
	
	/***
	 * Returns the individual fields of the board
	 * @return Field[] array with fields
	 */
	public Field[] getBoard() {
		return board;
	}
}
