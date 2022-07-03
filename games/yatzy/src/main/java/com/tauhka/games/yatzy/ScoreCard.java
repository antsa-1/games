package com.tauhka.games.yatzy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import jakarta.json.bind.annotation.JsonbProperty;

/**
 * @author antsa-1 from GitHub 14 May 2022
 **/
public class ScoreCard implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final int BONUS = 50;
	private static final int MIN_POINT_REQUIREMENT_FOR_BONUS = 63;
	private static final int ZIP_NADA = 0;

	private Map<HandType, Hand> hands;
	@JsonbProperty(value = "total")
	private Integer total;
	@JsonbProperty(value = "subTotal")
	private Integer subTotal;
	@JsonbProperty(value = "bonus")
	private Integer bonus;
	@JsonbProperty(value = "name")
	private String name;
	@JsonbProperty(value = "lastAdded")
	private Hand lastAdded;

	public ScoreCard() {
		hands = new HashMap<HandType, Hand>();
	}

	public Integer calculateTotal() {
		return hands.values().stream().mapToInt(hand -> hand.getValue()).sum();
	}

	public Integer calculateSubTotal() {
		return hands.values().stream().mapToInt(handu -> handu.calculateSubTotalPoints()).sum();
	}

	public Integer calculateBonus() {
		return calculateSubTotal() >= MIN_POINT_REQUIREMENT_FOR_BONUS ? BONUS : ZIP_NADA;
	}

	public Integer getTotal() {
		return total;
	}

	public Map<HandType, Hand> getHands() {
		return hands;
	}

	public void setHands(Map<HandType, Hand> hands) {
		this.hands = hands;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public Integer getSubTotal() {
		return subTotal;
	}

	public void setSubTotal(Integer subTotal) {
		this.subTotal = subTotal;
	}

	public Integer getBonus() {
		return bonus;
	}

	public void setBonus(Integer bonus) {
		this.bonus = bonus;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Hand getLastAdded() {
		return lastAdded;
	}

	public void setLastAdded(Hand lastAdded) {
		this.lastAdded = lastAdded;
	}

	public boolean hasEmptySlotForPairKinds() {
		return !getHands().containsKey(HandType.PAIR) || !getHands().containsKey(HandType.TWO_PAIR) || !getHands().containsKey(HandType.THREE_OF_KIND) || !getHands().containsKey(HandType.FULL_HOUSE)
				|| !getHands().containsKey(HandType.FOUR_OF_KIND) || !getHands().containsKey(HandType.YATZY);
	}

	public void addHand(Hand hand) {
		hand.setTypeNumber(hand.getHandType().getAsInt());
		var key = hands.putIfAbsent(hand.getHandType(), hand);
		if (key != null) {
			throw new IllegalArgumentException("HandDuplicate for:" + hand);
		}
		total = calculateTotal();
		subTotal = calculateSubTotal();
		bonus = calculateBonus();
		lastAdded = hand;
	}

	public boolean hasEmptySlotForCurrentNumber(int actualNumber) {
		// Ones, Twos, threes etc..
		HandType type = HandType.getHandType(actualNumber);
		return !this.getHands().containsKey(type);
	}
}
