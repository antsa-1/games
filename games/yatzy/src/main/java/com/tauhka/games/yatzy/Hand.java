package com.tauhka.games.yatzy;

import java.util.ArrayList;
import java.util.List;

import jakarta.json.bind.annotation.JsonbProperty;

/**
 * @author antsa-1 from GitHub 12 May 2022
 **/

public class Hand {
	@JsonbProperty("dices")
	private List<Dice> dices;
	@JsonbProperty("dices")
	private HandType handType;
	@JsonbProperty("value")
	private Integer value;

	private Hand() {

	}

	public Hand(HandType handType, List<Dice> tableDices) {
		dices = new ArrayList<Dice>(5);
		for (Dice d : tableDices) {
			Dice dice = new Dice();
			dice.setNumber(d.getNumber());
			dices.add(d);
		}
		this.handType = handType;
	}

	public Hand(HandType handType, List<Dice> tableDices, int value) {
		for (Dice d : tableDices) {
			Dice dice = new Dice();
			dice.setNumber(d.getNumber());
			dices.add(d);
		}
		this.handType = handType;
	}

	public Integer getValue() {
		return value;
	}

	public int calculateSubTotalPoints() {
		if (HandType.isSubTotalType(handType)) {
			return value;
		}
		return 0;
	}

	public List<Dice> getDices() {
		return dices;
	}

	public HandType getHandType() {
		return handType;
	}

}
