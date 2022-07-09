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
	@JsonbTransient
	private int count;

	public Hand(HandType handType, List<Dice> tableDices) {
		dices = new ArrayList<Dice>(5);
		for (Dice d : tableDices) {
			Dice dice = new Dice();
			dice.setNumber(d.getNumber());
			dices.add(d);
			count += dice.getNumber();
		}
		this.handType = handType;
		if (handType != null)
			this.value = HandCalculator.calculateHandValue(this);
	}

	public Hand(List<Dice> tableDices) {
		dices = new ArrayList<Dice>(5);
		for (Dice d : tableDices) {
			Dice dice = new Dice();
			dice.setNumber(d.getNumber());
			dices.add(d);
			count += dice.getNumber();
		}
	}

	public void setHandType(HandType handType) {
		this.handType = handType;
	}

	public Integer getValue() {
		if (value == null)
			value = HandCalculator.calculateHandValue(this);
		return value;
	}

	public Integer getTypeNumber() {
		return handType.getAsInt();
	}
	@JsonbTransient
	public boolean isYatzy() {
		return this.handType == HandType.YATZY;
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
	@JsonbTransient
	public boolean isStraight() {
		return this.handType == HandType.SMALL_STRAIGHT || this.handType == HandType.LARGE_STRAIGHT;
	}
	@JsonbTransient
	public boolean isSixes() {
		return this.handType == HandType.SIXES;
	}
	@JsonbTransient
	public boolean isFives() {
		return this.handType == HandType.FIVES;
	}
	@JsonbTransient
	public boolean isFullHouse() {
		return this.handType == HandType.FULL_HOUSE;
	}
	@JsonbTransient
	public boolean isFourOfKind() {
		return this.handType == HandType.FOUR_OF_KIND;
	}
	@JsonbTransient
	public boolean isPairOrTrips() {
		return this.handType == HandType.PAIR || this.handType == HandType.THREE_OF_KIND;
	}

	public Integer getCount() {
		return count;
	}

	@Override
	public String toString() {
		return "Hand [dices=" + dices + ", handType=" + handType + ", value=" + value + "]";
	}

}
