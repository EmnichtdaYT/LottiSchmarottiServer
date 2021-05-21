package de.emnichtda.lottischmarotti.server.entitys.game;

import java.util.Random;

public class Dice {
	
	private static Dice instance;
	
	private Random r = new Random();
	
	private Dice() { }
	
	public static Dice getInstance() {
		if(instance == null) instance = new Dice();
		return instance;
	}
	
	public int roll() {
		return r.nextInt(6)+1;
	}
	
}
