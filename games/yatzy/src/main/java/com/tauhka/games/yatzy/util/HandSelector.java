package com.tauhka.games.yatzy.util;

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
		HandWrapper handWrapper = new HandWrapper(null);
		if (!sc.getHands().containsKey(HandType.YATZY)) {
			handWrapper = verifyMostValuableHand(dices, HandType.YATZY, handWrapper);
		}
		if (!sc.getHands().containsKey(HandType.SMALL_STRAIGHT)) {
			handWrapper = verifyMostValuableHand(dices, HandType.SMALL_STRAIGHT, handWrapper);
		}
		if (!sc.getHands().containsKey(HandType.LARGE_STRAIGHT)) {
			handWrapper = verifyMostValuableHand(dices, HandType.LARGE_STRAIGHT, handWrapper);
		}
		if (!sc.getHands().containsKey(HandType.ONES)) {
			handWrapper = verifyMostValuableHand(dices, HandType.ONES, handWrapper);
		}
		if (!sc.getHands().containsKey(HandType.TWOS)) {
			handWrapper = verifyMostValuableHand(dices, HandType.TWOS, handWrapper);
		}
		if (!sc.getHands().containsKey(HandType.THREES)) {
			handWrapper = verifyMostValuableHand(dices, HandType.THREES, handWrapper);
		}
		if (!sc.getHands().containsKey(HandType.FOURS)) {
			handWrapper = verifyMostValuableHand(dices, HandType.FOURS, handWrapper);
		}
		if (!sc.getHands().containsKey(HandType.FIVES)) {
			handWrapper = verifyMostValuableHand(dices, HandType.FIVES, handWrapper);
		}
		if (!sc.getHands().containsKey(HandType.SIXES)) {
			handWrapper = verifyMostValuableHand(dices, HandType.SIXES, handWrapper);
		}
		if (!sc.getHands().containsKey(HandType.PAIR)) {
			handWrapper = verifyMostValuableHand(dices, HandType.PAIR, handWrapper);
		}
		if (!sc.getHands().containsKey(HandType.TWO_PAIR)) {
			handWrapper = verifyMostValuableHand(dices, HandType.TWO_PAIR, handWrapper);
		}
		if (!sc.getHands().containsKey(HandType.THREE_OF_KIND)) {
			handWrapper = verifyMostValuableHand(dices, HandType.THREE_OF_KIND, handWrapper);
		}
		if (!sc.getHands().containsKey(HandType.FULL_HOUSE)) {
			handWrapper = verifyMostValuableHand(dices, HandType.FULL_HOUSE, handWrapper);
		}

		if (!sc.getHands().containsKey(HandType.FOUR_OF_KIND)) {
			handWrapper = verifyMostValuableHand(dices, HandType.FOUR_OF_KIND, handWrapper);
		}
		if (!sc.getHands().containsKey(HandType.CHANCE)) {
			handWrapper = verifyMostValuableHand(dices, HandType.CHANCE, handWrapper);
		}
		return handWrapper.getHand();
	}

	private static HandWrapper verifyMostValuableHand(List<Dice> dices, HandType handTypeToCheck, HandWrapper wrapper) {
		Hand handCandidate = new Hand(handTypeToCheck, dices);
		int value = HandCalculator.calculateHandValue(handCandidate);
		if (wrapper.getHand() == null || value > wrapper.getHand().getValue()) {
			wrapper.setHand(handCandidate);
			return wrapper;
		}
		return wrapper;
	}

}
