package com.tauhka.games.yatzy;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author antsa-1 from GitHub 12 May 2022
 **/

public class Dice {
	private final UUID diceId = UUID.randomUUID();
	private int value; // short
	private boolean locked;

	public int roll() {
		value = ThreadLocalRandom.current().nextInt(1, 6);
		return value;
	}

	public int getNumber() {
		return value;
	}

	public UUID getDiceId() {
		return diceId;
	}

	public boolean isLocked() {
		return locked;
	}

	public void lock() {
		this.locked = true;
	}
}
