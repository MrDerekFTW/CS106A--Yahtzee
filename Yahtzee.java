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
	
/** sets up yahtzee and plays it once */
	public void run() {
		//this.rgen.setSeed(1338);
		IODialog dialog = getDialog();
		nPlayers = dialog.readInt("Enter number of players (max 4)");
		playerNames = new String[nPlayers];
		for (int i = 1; i <= nPlayers; i++) {
			playerNames[i - 1] = dialog.readLine("Enter name for player " + i);
		}
		display = new YahtzeeDisplay(getGCanvas(), playerNames);
		playGame();
	}

/** loops through a single game of Yahtzee */
	private void playGame() {
		while(nTurnsPlayed < N_SCORING_CATEGORIES) {
			for (int i = 0; i < this.nPlayers; i++){
				this.playerTurn(i + 1);
			}
			this.nTurnsPlayed++;
		}
		for (int i = 0; i < this.nPlayers; i++) {
			this.calcExtraScores(i + 1);
		}
		int winner = this.getWinner(this.nPlayers);
		display.printMessage("Congratulations " + this.playerNames[winner-1] + ", you win!");
	}
	
/** runs through a single turn for player, including one roll, two re-rolls, and a category selection */
	private void playerTurn(int player) {
		this.drawDice();
		display.printMessage(this.playerNames[player-1] + "'s turn! Click 'Roll Dice' to begin.");
		display.waitForPlayerToClickRoll(player);
		this.currentDice = this.rollDice();
		this.drawDice();
		display.printMessage("Select the dice you wish to re-roll and click 'Roll Again.'");
		display.waitForPlayerToSelectDice();
		this.rerollDice();
		this.drawDice();
		display.printMessage("Select the dice you wish to re-roll and click 'Roll Again.'");
		display.waitForPlayerToSelectDice();
		this.rerollDice();
		this.drawDice();
		
/*
 		//EXTRA ROLLS FOR TESTING START
		display.waitForPlayerToSelectDice();
		this.rerollDice();
		this.drawDice();
		display.waitForPlayerToSelectDice();
		this.rerollDice();
		this.drawDice();
		//EXTRA ROLLS FOR TESTING END
*/		
		
		display.printMessage("Select a category for this roll...");
		int category = display.waitForPlayerToSelectCategory();
		while (this.isNewCategory(category, player) == false) {
			display.printMessage("You've already chosen that category, please try again.");
			category = display.waitForPlayerToSelectCategory();
		}
		int score = this.getScore(category, this.currentDice);
		this.updateScores(category, player, score);
		this.updateCategories(category, player);
	}

/** draws the dice as they were most recently rolled */
	private void drawDice() {
		display.displayDice(currentDice);
	}

/** rolls a fresh set of dice
 *  @return an int array of 5 random values between 1 & 6 */
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

/** @return a random int between 1-6  */
	private int rollDie() {
		return this.rgen.nextInt(1, 6);
	}

/** rerolls the current dice with fresh random values */
	private void rerollDice() {
		for (int i = 0; i < 5; i++) {
			if (this.isDieSelected(i)) {
				this.currentDice[i] = this.rollDie();
			}
		}
	}
	
/** @return true if die at index is selected, false if not */
	private boolean isDieSelected(int index) {
		return display.isDieSelected(index);
	}
	
/** calculates the score according to standard Yahtzee rules in the case that dice
 * is applied to category (enumerated in YahtzeeConstants)
 * @return int representing the score for that category */
	private int getScore(int category, int[] dice) {
		switch (category) {
		case ONES:
			return this.sumTargetDice(dice, 1);
		case TWOS:
			return this.sumTargetDice(dice, 2);
		case THREES:
			return this.sumTargetDice(dice, 3);
		case FOURS:
			return this.sumTargetDice(dice, 4);
		case FIVES:
			return this.sumTargetDice(dice, 5);
		case SIXES:
			return this.sumTargetDice(dice, 6);
		case THREE_OF_A_KIND:
			return this.sumOfAKind(dice, 3);
		case FOUR_OF_A_KIND:
			return this.sumOfAKind(dice, 4);
		case FULL_HOUSE:
			return this.scoreFullHouse(dice);
		case SMALL_STRAIGHT:
			return this.scoreSmStraight(dice);
		case LARGE_STRAIGHT:
			return this.scoreLgStraight(dice);
		case YAHTZEE: 
			return this.scoreYahtzee(dice);
		case CHANCE:
			return this.getArraySum(dice);
		default:
			return 27; 
		}
	}
	
/** Calculates the sum of all dice in the array of type target (i.e. if player rolls 3 twos, returns 6).
 * This is used for calculating the upper scores.
 * @return the sum of all dice in the array of type target  */
	private int sumTargetDice(int[] dice, int target) {
		int total = 0;
		for (int i = 0; i < dice.length; i++) {
			if (dice[i] == target) {
				total += target;
			}
		}
		return total;
	}
	
/** Calculates the score for X of a kind, determined by xKind. (i.e. xKind = 3 for three of a kind category)
 * @return score */
	private int sumOfAKind(int[] dice, int xKind) {
		int[] diceCounts = {0, 0, 0, 0, 0, 0}; // keeps track of amount of 1s, 2s, 3s, 4s, 5s, 6s
		for (int i = 0; i < dice.length; i++) {
			int curr = dice[i];
			diceCounts[curr-1] = diceCounts[curr-1] + 1;
		}
		for (int i = 0; i < diceCounts.length; i++) {
			if (diceCounts[i] >= xKind) {
				return xKind * (i+1);
			}
		}
		return 0;
	}
	
/** @return 25 if dice is a full house, 0 if not */
	private int scoreFullHouse(int[] dice) {
		if (this.isFullHouse(dice)) {
			return 25;
		}
		return 0;
	}
	
/** @return true if dice is a full house, false if not */
	private boolean isFullHouse(int[] dice) {
		int valueOne = 0;
		int valueTwo = 0;
		int totalOne = 0;
		int totalTwo = 0;
		for (int i = 0; i < dice.length; i++) {
			if (valueOne == 0) {
				valueOne = dice[i];
				totalOne++;
			}
			else if (dice[i] == valueOne) {
				totalOne++;
			}
			else if (valueTwo == 0) {
				valueTwo = dice[i];
				totalTwo++;
			}
			else if (dice[i] == valueTwo) {
				totalTwo++;
			}
			else {
				return false; // more than 2 unique dice in a roll cannot be a full house
			}
		}
		if (totalOne + totalTwo == 5) {
			return true;
		}
		return false;
	}
	
/** @return 30 if dice is a small straight, 0 if not */
	private int scoreSmStraight(int[] dice) {
		if (this.isStraight(dice, 3)) {
			return 30;
		}
		return 0;
	}
	
/** @return 40 if dice is a large straight, 0 if not */
	private int scoreLgStraight(int[] dice) {
		if (this.isStraight(dice, 4)) {
			return 40;
		}
		return 0;
	}
	
/** @return true if dice has a straight of length targetLength, false otherwise */
	private boolean isStraight(int[] dice, int targetLength) {
		int[] diceCounts = {0, 0, 0, 0, 0, 0}; // keeps track of amount of 1s, 2s, 3s, 4s, 5s, 6s
		for (int i = 0; i < dice.length; i++) {
			int curr = dice[i];
			diceCounts[curr-1] = diceCounts[curr-1] + 1;
		}
		int comboCounter = 0;
		for (int i = 0; i < diceCounts.length; i++) {
			if (diceCounts[i] > 0) {
				comboCounter++;
			}
			else {
				comboCounter = 0;
			}
			if (comboCounter >= targetLength) {
				return true;
			}
		}
		return false;
	}

/** @return 50 is Yahtzee, 0 if not*/
	private int scoreYahtzee(int[] dice) {
		if (this.isYahtzee(dice)) {
			return 50;
		}
		return 0;
	}
	
/** @return true if yahtzee, false if not */
	private boolean isYahtzee(int[] dice) {
		int value = 0; // number on die
		int total = 0; // total number of dice with value
		for (int i = 0; i < dice.length; i++) {
			if (value == 0) {
				value = dice[i];
				total++;
			}
			else if (value == dice[i]) {
				total++;
			}
			else {
				return false;
			}
		}
		if (total == 5) {
			return true;
		}
		return false;
	}

/** @return sum of all integers in an int array */
	private int getArraySum(int[] array) {
		int total = 0;
		for (int i = 0; i < array.length; i++) {
			total += array[i];
		}
		return total;
	}
	
/** @return false if player has already chosen the category before, true if they haven't */
	private boolean isNewCategory(int category, int player) {
		return !chosenCategories[player-1][category];
	}
	
/** sets a category as already chosen by the player */
	private void updateCategories(int category, int player) {
		this.chosenCategories[player-1][category] = true;
	}
	
/** sets the score for player in a particular category */
	private void updateScores(int category, int player, int score) {
		this.scores[player][category-1] = score;
		display.updateScorecard(category, player, score);
	}
	
/** calculates the upper score, upper bonus, lower score, and total, and updates the scores accordingly */
	private void calcExtraScores(int player) {
		this.scores[player][UPPER_SCORE-1] = // -1 b/c provided constants begin at 0 s.t. array representation of scores is off by 1
			(this.scores[player][ONES-1] + this.scores[player][TWOS-1] + this.scores[player][THREES-1] +
				this.scores[player][FOURS-1] + this.scores[player][FIVES-1] + this.scores[player][SIXES-1]);
		display.updateScorecard(UPPER_SCORE, player, this.scores[player][UPPER_SCORE-1]);
		if (this.scores[player][UPPER_SCORE-1] >= 63) {
			this.scores[player][UPPER_BONUS-1] = 35;
			display.updateScorecard(UPPER_BONUS, player, 35);
		}
		this.scores[player][LOWER_SCORE-1] = 
			(this.scores[player][THREE_OF_A_KIND-1] + this.scores[player][FOUR_OF_A_KIND-1] + 
				this.scores[player][FULL_HOUSE-1] +this.scores[player][SMALL_STRAIGHT-1] + 
				this.scores[player][LARGE_STRAIGHT-1] + this.scores[player][YAHTZEE-1] +
				this.scores[player][CHANCE-1]);
		display.updateScorecard(LOWER_SCORE, player, this.scores[player][LOWER_SCORE-1]);
		int[] temp = this.scores[player];
		this.scores[player][TOTAL-1] = this.scores[player][UPPER_SCORE-1] + this.scores[player][UPPER_BONUS-1] + this.scores[player][LOWER_SCORE-1]; 
		this.updateScores(TOTAL, player, this.scores[player][TOTAL-1]);
	}

/** @return player with the highest score */
	private int getWinner(int numPlayers) {
		if (numPlayers == 1) {
			return 1;
		}
		if (numPlayers == 2) {
			if (this.scores[1][TOTAL-1] > this.scores[2][TOTAL-1]) {
				return 1;
			}
			else {
				return 2;
			}
		}
		if (numPlayers == 3) {
			int highestScore = this.scores[1][TOTAL-1];
			int winner = 1;
			if (this.scores[2][TOTAL-1] > highestScore) {
				highestScore = this.scores[2][TOTAL-1];
				winner = 2;
			}
			if (this.scores[3][TOTAL-1] > highestScore) {
				highestScore = this.scores[3][TOTAL-1];
				winner = 3;
			}
			return winner;
		}
		if (numPlayers == 4) {
			int highestScore = this.scores[1][TOTAL-1];
			int winner = 1;
			if (this.scores[2][TOTAL-1] > highestScore) {
				highestScore = this.scores[2][TOTAL-1];
				winner = 2;
			}
			if (this.scores[3][TOTAL-1] > highestScore) {
				highestScore = this.scores[3][TOTAL-1];
				winner = 3;
			}
			if (this.scores[4][TOTAL-1] > highestScore) {
				highestScore = this.scores[4][TOTAL-1];
				winner = 4;
			}
			return winner;
		}
		return 1;
	}
	
		
/* Private instance variables */
	private int[] currentDice = {0, 0, 0, 0, 0};
	private boolean[][] chosenCategories = {
		{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
		{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
		{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
		{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}
	};
	private int[][] scores = {
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
	};
	private int nPlayers;
	private int nTurnsPlayed = 0;
	private String[] playerNames;
	private YahtzeeDisplay display;
	private RandomGenerator rgen = new RandomGenerator();

}
