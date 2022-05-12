package com.tauhka.games.yatzy;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author antsa-1 from GitHub 12 May 2022
 **/

public class Dice {
	private int number; // short

	public int roll() {
		number = ThreadLocalRandom.current().nextInt(1, 6);
		return number;
	}

	public int getNumber() {
		return number;
	}

}
