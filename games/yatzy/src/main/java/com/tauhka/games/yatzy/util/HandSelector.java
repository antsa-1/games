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

	public static HandWrapper getMostValuableHands(ScoreCard sc, List<Dice> dices) {
		HandWrapper handWrapper = new HandWrapper(null);
		if (!sc.getHands().containsKey(HandType.YATZY)) {
			handWrapper = verifyMostValuableHands(dices, HandType.YATZY, handWrapper);
		}
		if (!sc.getHands().containsKey(HandType.SMALL_STRAIGHT)) {
			handWrapper = verifyMostValuableHands(dices, HandType.SMALL_STRAIGHT, handWrapper);
		}
		if (!sc.getHands().containsKey(HandType.LARGE_STRAIGHT)) {
			handWrapper = verifyMostValuableHands(dices, HandType.LARGE_STRAIGHT, handWrapper);
		}
		if (!sc.getHands().containsKey(HandType.ONES)) {
			handWrapper = verifyMostValuableHands(dices, HandType.ONES, handWrapper);
		}
		if (!sc.getHands().containsKey(HandType.TWOS)) {
			handWrapper = verifyMostValuableHands(dices, HandType.TWOS, handWrapper);
		}
		if (!sc.getHands().containsKey(HandType.THREES)) {
			handWrapper = verifyMostValuableHands(dices, HandType.THREES, handWrapper);
		}
		if (!sc.getHands().containsKey(HandType.FOURS)) {
			handWrapper = verifyMostValuableHands(dices, HandType.FOURS, handWrapper);
		}
		if (!sc.getHands().containsKey(HandType.FIVES)) {
			handWrapper = verifyMostValuableHands(dices, HandType.FIVES, handWrapper);
		}
		if (!sc.getHands().containsKey(HandType.SIXES)) {
			handWrapper = verifyMostValuableHands(dices, HandType.SIXES, handWrapper);
		}
		if (!sc.getHands().containsKey(HandType.PAIR)) {
			handWrapper = verifyMostValuableHands(dices, HandType.PAIR, handWrapper);
		}
		if (!sc.getHands().containsKey(HandType.TWO_PAIR)) {
			handWrapper = verifyMostValuableHands(dices, HandType.TWO_PAIR, handWrapper);
		}
		if (!sc.getHands().containsKey(HandType.THREE_OF_KIND)) {
			handWrapper = verifyMostValuableHands(dices, HandType.THREE_OF_KIND, handWrapper);
		}
		if (!sc.getHands().containsKey(HandType.FULL_HOUSE)) {
			handWrapper = verifyMostValuableHands(dices, HandType.FULL_HOUSE, handWrapper);
		}

		if (!sc.getHands().containsKey(HandType.FOUR_OF_KIND)) {
			handWrapper = verifyMostValuableHands(dices, HandType.FOUR_OF_KIND, handWrapper);
		}
		if (!sc.getHands().containsKey(HandType.CHANCE)) {
			handWrapper = verifyMostValuableHands(dices, HandType.CHANCE, handWrapper);
		}
		if (handWrapper.getSecondMostValuable() == null) {
			// In last hand there is only one possibility
			handWrapper.setSecondMostValuable(handWrapper.getMostValuable());
		}
		return handWrapper;
	}

	private static HandWrapper verifyMostValuableHands(List<Dice> dices, HandType handTypeToCheck, HandWrapper wrapper) {
		Hand handCandidate = new Hand(handTypeToCheck, dices);
		int value = HandCalculator.calculateHandValue(handCandidate);
		if (wrapper.getMostValuable() == null || value > wrapper.getMostValuable().getValue()) {
			wrapper.setSecondMostValuable(wrapper.getMostValuable());
			wrapper.setMostValuable(handCandidate);
			return wrapper;
		}
		if (handCandidate.getHandType() != wrapper.getMostValuable().getHandType()) {
			if (wrapper.getSecondMostValuable() == null || value > wrapper.getSecondMostValuable().getValue()) {
				wrapper.setSecondMostValuable(handCandidate);
				return wrapper;
			}
		}
		return wrapper;
	}

}
