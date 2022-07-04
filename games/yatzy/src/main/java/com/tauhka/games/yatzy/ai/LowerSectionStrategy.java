package com.tauhka.games.yatzy.ai;

import java.util.List;
import java.util.Set;

import com.tauhka.games.yatzy.Dice;
import com.tauhka.games.yatzy.Hand;
import com.tauhka.games.yatzy.HandType;
import com.tauhka.games.yatzy.ScoreCard;
import com.tauhka.games.yatzy.YatzyAI;
import com.tauhka.games.yatzy.util.HandCalculator;

/**
 * @author antsa-1 from GitHub 4 Jul 2022
 **/

public class LowerSectionStrategy {
	public ContemplationResult deepContemplateHands(ScoreCard scoreCard, List<Dice> dices, YatzyAI yatzyComputer) {
		Hand h = new Hand(dices);
		if (scoreCard.isMissingHandType(HandType.FULL_HOUSE)) {
			int fullHousePoints = HandCalculator.findFullHouse(h);
			if (fullHousePoints > 18) {
				h.setHandType(HandType.FULL_HOUSE);
				return createResult(h, dices);
			}
			if (scoreCard.handsLeft() < 8 && fullHousePoints > 0) {
				// Take full house, 7 hands left to play
				h.setHandType(HandType.FULL_HOUSE);
				return createResult(h, dices);
			}
		}

		if (scoreCard.isMissingHandType(HandType.SMALL_STRAIGHT)) {
			if (HandCalculator.isSmallStraight(h)) {
				h.setHandType(HandType.SMALL_STRAIGHT);
				return createResult(h, dices);
			}
		}
		if (scoreCard.isMissingHandType(HandType.LARGE_STRAIGHT)) {
			if (HandCalculator.isLargeStraight(h)) {
				h.setHandType(HandType.LARGE_STRAIGHT);
				return createResult(h, dices);
			}
		}

		if (scoreCard.isMissingHandType(HandType.YATZY)) {
			int number = HandCalculator.getNumberWhichExistAtLeastTimes(5, h);
			if (number > 0) {
				h.setHandType(HandType.YATZY);
				return createResult(h, dices);

			}
		}
		if (scoreCard.isMissingHandType(HandType.FOUR_OF_KIND) || scoreCard.isMissingHandType(HandType.YATZY)) {
			int number = HandCalculator.getNumberWhichExistAtLeastTimes(4, h); // Tries to get Yatzy even if scoreCard has 4 of a kind, but missing yatzy
			if (number > 0) {
				if (yatzyComputer.hasRollsLeft()) {
					yatzyComputer.selectNumbers(dices, number);
					return createResult(h, dices);
				} else if (scoreCard.isMissingHandType(HandType.FOUR_OF_KIND)) {
					h.setHandType(HandType.FOUR_OF_KIND);
					return createResult(h, dices);
				}
			}
		}
		if (scoreCard.isMissingHandType(HandType.THREE_OF_KIND) || scoreCard.isMissingHandType(HandType.FOUR_OF_KIND)) {
			int number = HandCalculator.getNumberWhichExistAtLeastTimes(3, h);
			if (number > 0) {
				if (yatzyComputer.hasRollsLeft()) {
					yatzyComputer.selectNumbers(dices, number);
					return createResult(h, dices);
				} else if (scoreCard.isMissingHandType(HandType.THREE_OF_KIND)) {
					h.setHandType(HandType.THREE_OF_KIND);
					return createResult(h, dices);
				}
			}
		}
		if (scoreCard.isMissingHandType(HandType.TWO_PAIR) || scoreCard.isMissingHandType(HandType.THREE_OF_KIND) || scoreCard.isMissingHandType(HandType.FULL_HOUSE)) {
			Set<Integer> pairs = HandCalculator.searchAllPairsFromBiggestToSmallest(h);
			if (pairs.size() == 2) {
				handleTwoPairs(dices, yatzyComputer, h, pairs, scoreCard);
				return createResult(h, dices);
			}
		}
		if (scoreCard.isMissingHandType(HandType.CHANCE)) {
			int count = HandCalculator.addAllNumbers(h);
			if (count > 22) {
				h.setHandType(HandType.CHANCE);
				return createResult(h, dices);
			}
			if (count > 14 && yatzyComputer.hasRollsLeft()) {
				yatzyComputer.selectNumbers(dices, 6);
				yatzyComputer.selectNumbers(dices, 5);
				yatzyComputer.selectNumbers(dices, 4);
				return createResult(h, dices);
			}
		}
		if (scoreCard.isMissingHandType(HandType.PAIR) || scoreCard.isMissingHandType(HandType.TWO_PAIR) || scoreCard.isMissingHandType(HandType.THREE_OF_KIND)) {
			Set<Integer> pairs = HandCalculator.searchAllPairsFromBiggestToSmallest(h);
			if (pairs.size() == 1) {
				int first = (int) pairs.toArray()[0];
				if (yatzyComputer.hasRollsLeft() && first < 3) {
					return createResult(h, dices);
				} else if (yatzyComputer.hasRollsLeft() && first >= 3) {
					yatzyComputer.selectNumbers(dices, first);
					return createResult(h, dices);
				} else if (!yatzyComputer.hasRollsLeft()) {
					if (scoreCard.isMissingPair()) {
						h.setHandType(HandType.PAIR);
					}
					return createResult(h, dices);
				}
			}
			if (pairs.size() == 2) {
				handleTwoPairs(dices, yatzyComputer, h, pairs, scoreCard);
				return createResult(h, dices);
			}
		}
		return createResult(h, dices);
	}

	private ContemplationResult createResult(Hand h, List<Dice> dices) {
		ContemplationResult c = new ContemplationResult();
		c.setHand(h);
		c.setCopies(dices);
		return c;
	}

	private Hand handleTwoPairs(List<Dice> dices, YatzyAI yatzyComputer, Hand h, Set<Integer> pairs, ScoreCard scoreCard) {
		int first = (int) pairs.toArray()[0];
		int second = (int) pairs.toArray()[1];
		if (yatzyComputer.hasRollsLeft()) {
			yatzyComputer.selectNumbers(dices, first);
			yatzyComputer.selectNumbers(dices, second);
			return h;
		} else if (scoreCard.isMissingTwoPair()) {
			h.setHandType(HandType.TWO_PAIR);
		}
		return h;
	}

}
