package com.tauhka.games.yatzy;

import java.util.HashMap;
import java.util.Map;

import jakarta.json.bind.annotation.JsonbProperty;

/**
 * @author antsa-1 from GitHub 14 May 2022
 **/

public class ScoreCard {
	private static final int BONUS = 50;
	private static final int MIN_POINTS_FOR_BONUS = 63;
	private static final int ZIP_NADA = 0;
	@JsonbProperty(value = "hands")
	private Map<HandType, Hand> hands;
	@JsonbProperty(value = "total")
	private Integer total = calculateTotal();
	@JsonbProperty(value = "subTotal")
	private Integer subTotal = calculateSubTotal();
	@JsonbProperty(value = "bonus")
	private Integer bonus = calculateBonus();
	@JsonbProperty(value = "lastAdded")
	private HandType handType;

	public Integer calculateTotal() {
		return hands.values().stream().mapToInt(hand -> hand.calculatePoints()).sum();
	}

	public Integer calculateSubTotal() {
		return hands.values().stream().mapToInt(handu -> handu.calculateSubTotalPoints()).sum();
	}

	public Integer calculateBonus() {
		return calculateSubTotal() >= MIN_POINTS_FOR_BONUS ? BONUS : ZIP_NADA;
	}

	public HandType getHandType() {
		return handType;
	}

	public ScoreCard() {
		hands = new HashMap<HandType, Hand>();
	}

	public void setHand(HandType handType, Hand hand) {
		hands.put(handType, hand);
	}
}
