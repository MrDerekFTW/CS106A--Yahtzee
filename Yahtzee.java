/*
 * File: Yahtzee.java
 * ------------------
 * This program will eventually play the Yahtzee game.
 */

import acm.io.*;
import acm.program.*;
import acm.util.*;

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants {
	
	public static void main(String[] args) {
		new Yahtzee().start(args);
	}
	
	public void run() {
		IODialog dialog = getDialog();
		nPlayers = dialog.readInt("Enter number of players");
		playerNames = new String[nPlayers];
		for (int i = 1; i <= nPlayers; i++) {
			playerNames[i - 1] = dialog.readLine("Enter name for player " + i);
		}
		display = new YahtzeeDisplay(getGCanvas(), playerNames);
		playGame();
	}

	private void playGame() {
		while(true) {
			this.playerTurn(1);
		}
	}
	
	private void playerTurn(int player) {
		this.drawDice();
		display.waitForPlayerToClickRoll(player);
		this.currentDice = this.rollDice();
		this.drawDice();
		display.waitForPlayerToSelectDice();
		this.rerollDice();
		this.drawDice();
		display.waitForPlayerToSelectDice();
		this.rerollDice();
		this.drawDice();
		int category = display.waitForPlayerToSelectCategory();
		if (YahtzeeMagicStub.checkCategory(this.currentDice, category)) {
			int score = this.getScore(category, this.currentDice);
			display.updateScorecard(category, player, score);
		}
	}
	
	private void drawDice() {
		display.displayDice(currentDice);
	}
	
	private int[] rollDice() {
		int[] dArray = {
				this.rollDie(),
				this.rollDie(),
				this.rollDie(),
				this.rollDie(),
				this.rollDie()
		};
		return dArray;
	}
	
	private int rollDie() {
		return this.rgen.nextInt(1, 6);
	}
	
	private void rerollDice() {
		for (int i = 0; i < 5; i++) {
			if (this.isDieSelected(i)) {
				this.currentDice = this.rerollDie(i);
			}
		}
	}
	
	private int[] rerollDie(int i) {
		switch (i) {
		case 0:
			int[] a = {
				this.rollDie(),
				this.currentDice[1],
				this.currentDice[2],
				this.currentDice[3],
				this.currentDice[4]
			};
			return a;
		case 1: 
			int[] b = {
				this.currentDice[0],
				this.rollDie(),
				this.currentDice[2],
				this.currentDice[3],
				this.currentDice[4]
			};
			return b;
		case 2: 
			int[] c = {
				this.currentDice[0],
				this.currentDice[1],
				this.rollDie(),
				this.currentDice[3],
				this.currentDice[4]
			};
			return c;
		case 3: 
			int[] d = {
				this.currentDice[0],
				this.currentDice[1],
				this.currentDice[2],
				this.rollDie(),
				this.currentDice[4]
			};
			return d;
		case 4: 
			int[] e = {
				this.currentDice[0],
				this.currentDice[1],
				this.currentDice[2],
				this.currentDice[3],
				this.rollDie()
			};
			return e;
		default: 
			return this.currentDice;
		}
			
	}
	
	private boolean isDieSelected(int index) {
		return display.isDieSelected(index);
	}
	
	
	// LEFT OFF HERE
	private int getScore(int category, int[] dice) {
		switch (category) {
		//case ONES: case TWOS: case THREES:
		//case FOURS: case FIVES: case SIXES:
		case YAHTZEE: case CHANCE:
			return this.getArraySum(dice);
		default:
			return 27;
		}
	}
	
	private int getArraySum(int[] array) {
		int total = 0;
		for (int i = 0; i < array.length; i++) {
			total += array[i];
		}
		return total;
	}

	
		
/* Private instance variables */
	private int[] currentDice = {0, 0, 0, 0, 0};
	private int nPlayers;
	private String[] playerNames;
	private YahtzeeDisplay display;
	private RandomGenerator rgen = new RandomGenerator();

}
