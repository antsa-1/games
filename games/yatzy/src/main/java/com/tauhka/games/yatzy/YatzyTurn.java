package com.tauhka.games.yatzy;

import java.io.Serializable;
import java.util.List;

import jakarta.json.bind.annotation.JsonbProperty;

/**
 * @author antsa-1 from GitHub 6 Jun 2022
 **/

public class YatzyTurn implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonbProperty(value = "diceIds")
	private List<String> diceIds; // Input
	@JsonbProperty(value = "handType")
	private HandType handType;

	@JsonbProperty(value = "dices")
	private List<Dice> dices; // Output with changed values

	public List<String> getDiceIds() {
		return diceIds;
	}

	public void setDiceIds(List<String> diceIds) {
		this.diceIds = diceIds;
	}

	public List<Dice> getDices() {
		return dices;
	}

	public void setDices(List<Dice> dices) {
		this.dices = dices;
	}

	public HandType getHandType() {
		return handType;
	}

	public void setHandType(HandType handType) {
		this.handType = handType;
	}

}
