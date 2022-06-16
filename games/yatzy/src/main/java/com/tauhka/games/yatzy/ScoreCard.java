package com.tauhka.games.yatzy;

import java.util.HashMap;
import java.util.Map;

import jakarta.json.bind.annotation.JsonbProperty;

/**
 * @author antsa-1 from GitHub 14 May 2022
 **/

public class ScoreCard {
	private static final int BONUS = 50;
	private static final int MIN_POINT_REQUIREMENT_FOR_BONUS = 63;
	private static final int ZIP_NADA = 0;
	@JsonbProperty(value = "hands")
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

	public void addHand(Hand hand) {
		hands.put(hand.getHandType(), hand);
		total = calculateTotal();
		subTotal = calculateSubTotal();
		bonus = calculateBonus();
		lastAdded = hand;
	}
}
