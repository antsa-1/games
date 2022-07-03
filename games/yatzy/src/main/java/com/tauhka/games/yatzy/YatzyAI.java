package com.tauhka.games.yatzy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.tauhka.games.core.ai.AI;
import com.tauhka.games.yatzy.util.HandCalculator;
import com.tauhka.games.yatzy.util.HandSelector;
import com.tauhka.games.yatzy.util.HandWrapper;

/**
 * @author antsa-1 from GitHub 2 Jul 2022
 **/

public class YatzyAI extends YatzyPlayer implements AI {

	private static final long serialVersionUID = 1L;

	public Hand deepContemplateHands(ScoreCard scoreCard, List<Dice> dices, int rollsLeft) {
		List<Integer> numbers = dices.stream().map(Dice::getNumber).collect(Collectors.toList());
		Set<Integer> individualNumbersInOrder = new TreeSet<Integer>(Set.copyOf(numbers));
		// return immediately hands if they are not in scoreCard
		Hand h = new Hand(dices);
		if (isFullHouseMissingFromScoreCard(scoreCard, h)) {
			h.setHandType(HandType.FULL_HOUSE);
			selectAllDices(dices);
			return h;
		}

		if (isSmallStraightMissingFromScoreCard(scoreCard, h)) {
			h.setHandType(HandType.SMALL_STRAIGHT);
			selectAllDices(dices);
			return h;
		}
		if (isLargeStraightMissingFromScoreCard(scoreCard, h)) {
			h.setHandType(HandType.LARGE_STRAIGHT);
			selectAllDices(dices);
			return h;
		}
		if (individualNumbersInOrder.size() == 1) {
			if (!scoreCard.getHands().containsKey(HandType.YATZY)) {
				h.setHandType(HandType.YATZY);
				selectAllDices(dices);
				return h;
			}
		}
		if (isDecentFourOfKindMissingFromScoreCard(scoreCard, h, dices)) {
			h.setHandType(HandType.FOUR_OF_KIND);
			return h;
		}
		HandWrapper wrapper = HandSelector.getMostValuableHands(getScoreCard(), dices);
		if (rollsLeft == 0) {
			// It is what it is, not much left to be done
			if (wrapper.getSecondMostValuable() == null) {
				wrapper.getMostValuable();
			}
			if (wrapper.getMostValuable().getValue() > 20) {
				return wrapper.getMostValuable();
			}
			return wrapper.getSecondMostValuable();
		}
		if (wrapper.getMostValuable().getValue() > 20 && !scoreCardIsMissingHandType(scoreCard, HandType.CHANCE)) {
			return wrapper.getMostValuable();
		} else if (wrapper.getSecondMostValuable().getValue() >= 24) {
			return wrapper.getSecondMostValuable();
		}
		if (shouldSelectTwoPair(scoreCard, dices, h)) {
			return null;
		}
		int sixesCount = Collections.frequency(numbers, 6);
		if (selectInterestingDices(scoreCard, dices, sixesCount, 6)) {
			return null;
		}
		int fivesCount = Collections.frequency(numbers, 5);
		if (selectInterestingDices(scoreCard, dices, fivesCount, 5)) {
			return null;
		}
		int foursCount = Collections.frequency(numbers, 4);
		if (selectInterestingDices(scoreCard, dices, foursCount, 4)) {
			return null;
		}
		int threesCount = Collections.frequency(numbers, 3);
		if (selectInterestingDices(scoreCard, dices, threesCount, 3)) {
			return null;
		}
		int twosCount = Collections.frequency(numbers, 2);
		if (selectInterestingDices(scoreCard, dices, twosCount, 2)) {
			return null;
		}
		// Select nothing
		return null;
	}

	private boolean isDecentFourOfKindMissingFromScoreCard(ScoreCard scoreCard, Hand hand, List<Dice> dices) {
		if (scoreCard.getHands().containsKey(HandType.FOUR_OF_KIND)) {
			return false;
		}
		Integer number = HandCalculator.getNumberWhichExistAtLeastTimes(4, hand);
		if (number != null && number > 2) {
			for (Dice d : dices) {
				d.selectDice();
			}
			return true;
		}
		return false;
	}

//	public static void main(String[] args) {
//		ScoreCard sc = new ScoreCard();
//		Dice d1 = new Dice(3);
//		Dice d2 = new Dice(3);
//		Dice d3 = new Dice(3);
//		Dice d4 = new Dice(1);
//		Dice d5 = new Dice(1);
//
//		List<Dice> dices = new ArrayList<Dice>();
//		dices.add(d1);
//		dices.add(d2);
//		dices.add(d3);
//		dices.add(d4);
//		dices.add(d5);
//		Hand hand = new Hand(HandType.FULL_HOUSE, dices);
//		sc.addHand(hand);
//
//		Dice d6 = new Dice(3);
//		Dice d7 = new Dice(3);
//		Dice d8 = new Dice(3);
//		Dice d9 = new Dice(5);
//		Dice d10 = new Dice(5);
//
//		List<Dice> dices2 = new ArrayList<Dice>();
//		dices2.add(d6);
//		dices2.add(d7);
//		dices2.add(d8);
//		dices2.add(d9);
//		dices2.add(d10);
//		YatzyAI y = new YatzyAI();
//		Hand hand2 = new Hand(dices2);
//		boolean val = y.shouldSelectTwoPair(sc, dices2, hand2);
//		System.out.println("VAL:" + val);
//	}

	private boolean shouldSelectTwoPair(ScoreCard scoreCard, List<Dice> dices, Hand hand) {
		if (!scoreCardIsMissingHandType(scoreCard, HandType.TWO_PAIR) && !scoreCardIsMissingHandType(scoreCard, HandType.FULL_HOUSE)) {
			return false;
		}
		Set<Integer> pairs = HandCalculator.searchAllPairsFromBiggestToSmallest(hand);
		if (pairs.size() == 2) {
			int firstNumber = (int) pairs.toArray()[0];
			int secondNumber = (int) pairs.toArray()[1];
			int selectedCountNumber1 = 0;
			int selectedCountNumber2 = 0;
			for (Dice d : dices) { // 2-3 different numbers in various orders
				// Not allowed to select all dices without forming an actual hand -> leads to situation where action ="ROLL_DICES" but no dices to roll
				if (d.getNumber() == firstNumber && selectedCountNumber1 < 2) {
					d.selectDice();
					selectedCountNumber1++;
				} else if (d.getNumber() == secondNumber && selectedCountNumber2 < 2) {
					d.selectDice();
					selectedCountNumber2++;
				}
			}
			return true;
		}
		return false;
	}

	private boolean scoreCardIsMissingHandType(ScoreCard sc, HandType handType) {
		return !sc.getHands().containsKey(handType);
	}

	private boolean isLargeStraightMissingFromScoreCard(ScoreCard scoreCard, Hand h) {
		return !scoreCard.getHands().containsKey(HandType.LARGE_STRAIGHT) && HandCalculator.isSmallStraight(h);
	}

	private boolean isSmallStraightMissingFromScoreCard(ScoreCard scoreCard, Hand h) {
		return !scoreCard.getHands().containsKey(HandType.SMALL_STRAIGHT) && HandCalculator.isSmallStraight(h);
	}

	private boolean isFullHouseMissingFromScoreCard(ScoreCard scoreCard, Hand h) {
		return !scoreCard.getHands().containsKey(HandType.FULL_HOUSE) && HandCalculator.findFullHouse(h) > 0;
	}

	private void selectAllDices(List<Dice> dices) {
		for (Dice d : dices) {
			d.selectDice();
		}
	}

	private boolean selectInterestingDices(ScoreCard scoreCard, List<Dice> dices, int numberCount, int actualNumber) {
		if (numberCount >= 2 && (scoreCard.hasEmptySlotForPairKinds() || scoreCard.hasEmptySlotForCurrentNumber(actualNumber))) {
			dices.stream().filter(dice -> dice.getNumber() == actualNumber).forEach(dice -> dice.selectDice());
			return true;
		}
		return false;
	}
}
