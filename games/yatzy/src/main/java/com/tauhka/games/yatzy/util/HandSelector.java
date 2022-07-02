package com.tauhka.games.yatzy.util;

import java.util.Collections;
import java.util.List;

import com.tauhka.games.yatzy.Dice;
import com.tauhka.games.yatzy.Hand;
import com.tauhka.games.yatzy.HandType;
import com.tauhka.games.yatzy.ScoreCard;

/**
 * @author antsa-1 from GitHub 2 Jul 2022
 **/

public class HandSelector {

	public static Hand getBestHand(ScoreCard sc, List<Dice> dices) {
		Hand bestHand = null;
		if (!sc.getHands().containsKey(HandType.YATZY)) {
			bestHand = verifyMostValuableHand(dices, HandType.YATZY, bestHand);
		}
		if (!sc.getHands().containsKey(HandType.SMALL_STRAIGHT)) {
			bestHand = verifyMostValuableHand(dices, HandType.SMALL_STRAIGHT, bestHand);
		}
		if (!sc.getHands().containsKey(HandType.LARGE_STRAIGHT)) {
			bestHand = verifyMostValuableHand(dices, HandType.LARGE_STRAIGHT, bestHand);
		}
		if (!sc.getHands().containsKey(HandType.ONES)) {
			bestHand = verifyMostValuableHand(dices, HandType.ONES, bestHand);
		}
		if (!sc.getHands().containsKey(HandType.TWOS)) {
			bestHand = verifyMostValuableHand(dices, HandType.TWOS, bestHand);
		}
		if (!sc.getHands().containsKey(HandType.THREES)) {
			bestHand = verifyMostValuableHand(dices, HandType.THREES, bestHand);
		}
		if (!sc.getHands().containsKey(HandType.FOURS)) {
			bestHand = verifyMostValuableHand(dices, HandType.FOURS, bestHand);
		}
		if (!sc.getHands().containsKey(HandType.FIVES)) {
			bestHand = verifyMostValuableHand(dices, HandType.FIVES, bestHand);
		}
		if (!sc.getHands().containsKey(HandType.SIXES)) {
			bestHand = verifyMostValuableHand(dices, HandType.SIXES, bestHand);
		}
		if (!sc.getHands().containsKey(HandType.PAIR)) {
			bestHand = verifyMostValuableHand(dices, HandType.PAIR, bestHand);
		}
		if (!sc.getHands().containsKey(HandType.TWO_PAIR)) {
			bestHand = verifyMostValuableHand(dices, HandType.TWO_PAIR, bestHand);
		}
		if (!sc.getHands().containsKey(HandType.THREE_OF_KIND)) {
			bestHand = verifyMostValuableHand(dices, HandType.THREE_OF_KIND, bestHand);
		}
		if (!sc.getHands().containsKey(HandType.FULL_HOUSE)) {
			bestHand = verifyMostValuableHand(dices, HandType.FULL_HOUSE, bestHand);
		}

		if (!sc.getHands().containsKey(HandType.FOUR_OF_KIND)) {
			bestHand = verifyMostValuableHand(dices, HandType.FOUR_OF_KIND, bestHand);
		}
		if (!sc.getHands().containsKey(HandType.CHANCE)) {
			bestHand = verifyMostValuableHand(dices, HandType.CHANCE, bestHand);
		}
		return bestHand;
	}

	private static Hand verifyMostValuableHand(List<Dice> dices, HandType handTypeToCheck, Hand currentBestHand) {
		Hand handCandidate = new Hand(handTypeToCheck, dices);
		int value = HandCalculator.calculateHandValue(handCandidate);
		if (currentBestHand == null || value > currentBestHand.getValue()) {
			return handCandidate;
		}
		return currentBestHand;
	}

}
