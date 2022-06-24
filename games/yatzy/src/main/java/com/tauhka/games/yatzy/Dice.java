package com.tauhka.games.yatzy;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import jakarta.json.bind.annotation.JsonbProperty;

/**
 * @author antsa-1 from GitHub 12 May 2022
 **/

public class Dice {
	@JsonbProperty(value = "diceId")
	public UUID diceId;
	@JsonbProperty(value = "number")
	private int number;
	@JsonbProperty(value = "selected")
	private boolean selected;

	public Dice() {
		// empty constr. serializing purposes
	}

	public Dice(int number) {
		this.number = number;
	}

	public int roll() {
		number = ThreadLocalRandom.current().nextInt(1, 7);
		return number;
	}

	public UUID getDiceId() {
		return diceId;
	}

	public void setDiceId(UUID diceId) {
		this.diceId = diceId;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
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

	public Integer getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	@Override
	public String toString() {
		return "Dice [diceId=" + diceId + ", number=" + number + ", selected=" + selected + "]";
	}

}
