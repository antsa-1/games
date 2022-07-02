package com.tauhka.games.yatzy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.tauhka.games.yatzy.util.HandCalculator;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;

/**
 * @author antsa-1 from GitHub 12 May 2022
 **/

public class Hand implements Serializable {
	private static final long serialVersionUID = 1L;
	@JsonbTransient
	private List<Dice> dices;
	@JsonbProperty("handType")
	private HandType handType;
	@JsonbProperty("typeNumber")
	private Integer typeNumber;
	@JsonbProperty("value")
	private Integer value;

	public Hand(HandType handType, List<Dice> tableDices) {
		dices = new ArrayList<Dice>(5);
		for (Dice d : tableDices) {
			Dice dice = new Dice();
			dice.setNumber(d.getNumber());
			dices.add(d);
		}
		this.handType = handType;
		this.value = HandCalculator.calculateHandValue(this);
	}

	public Hand(HandType handType, List<Dice> tableDices, int value) {
		for (Dice d : tableDices) {
			Dice dice = new Dice();
			dice.setNumber(d.getNumber());
			dices.add(d);
		}
		this.handType = handType;
	}

	public void setHandType(HandType handType) {
		this.handType = handType;
	}

	public Integer getValue() {
		return value;
	}

	public Integer getTypeNumber() {
		return handType.getAsInt();
	}

	public void setTypeNumber(Integer typeNumber) {
		this.typeNumber = typeNumber;
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

	@Override
	public String toString() {
		return "Hand [dices=" + dices + ", handType=" + handType + ", value=" + value + "]";
	}

}
