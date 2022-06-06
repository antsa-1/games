package com.tauhka.games.yatzy;

import java.util.Objects;
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

	@Override
	public int hashCode() {
		return Objects.hash(diceId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Dice other = (Dice) obj;
		return Objects.equals(diceId, other.diceId);
	}

	@Override
	public String toString() {
		return "Dice [diceId=" + diceId + ", value=" + value + ", locked=" + locked + "]";
	}

}
