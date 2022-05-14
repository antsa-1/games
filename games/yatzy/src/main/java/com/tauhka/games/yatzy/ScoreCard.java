package com.tauhka.games.yatzy;

import java.util.HashMap;
import java.util.Map;

/**
 * @author antsa-1 from GitHub 14 May 2022
 **/

public class ScoreCard {
	private Map<HandType, Hand> hands;
	private static final int BONUS = 50;
	private static final int MIN_POINTS_FOR_BONUS = 63;
	private static final int ZIP_NADA = 0;

	public Integer calculateTotal() {
		return hands.values().stream().mapToInt(hand -> hand.calculatePoints()).sum();
	}

	public Integer calculateSubTotal() {
		return hands.values().stream().mapToInt(handu -> handu.calculateSubTotalPoints()).sum();
	}

	public Integer calculateBonus() {
		return calculateSubTotal() >= MIN_POINTS_FOR_BONUS ? BONUS : ZIP_NADA;
	}

	public ScoreCard() {
		hands = new HashMap<HandType, Hand>();
	}

}
