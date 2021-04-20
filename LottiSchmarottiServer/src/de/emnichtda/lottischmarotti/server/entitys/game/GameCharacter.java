package de.emnichtda.lottischmarotti.server.entitys.game;

public class GameCharacter {
	private Field field;
	private Player owner;
	
	/***
	 * gets the field the character is currently standing on
	 * @return Field standing on
	 */
	public Field getField() {
		return field;
	}
	
	/***
	 * Update the characters information on where its standing currently 
	 * @param field where character is standing on
	 * @throws IllegalStateException if character is not standing on this field
	 */
	protected void setField(Field field) throws IllegalStateException{
		if(field!=null && field.getStanding() != this) throw new IllegalStateException("The new field is not the field the character is standing on! You can't use the method GameCharacter.setFiled() to change a characters position!");
		this.field = field;
	}

	/***
	 * get the player who owns this character
	 * @return Player owner
	 */
	public Player getOwner() {
		return owner;
	}

	/***
	 * Update the characters information on who owns it
	 * @param owner
	 * @throws IllegalStateException if owner doesnt own the character
	 */
	protected void setOwner(Player owner) throws IllegalStateException{
		if(!owner.isOwningCharacter(this)) throw new IllegalStateException("The new owner doesn't own the character.");
		this.owner = owner;
	}
	
	
}
