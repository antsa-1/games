package com.tauhka.games.yatzy;

import java.util.ArrayList;
import java.util.List;

import com.tauhka.games.core.ai.AI;
import com.tauhka.games.yatzy.ai.ContemplationResult;
import com.tauhka.games.yatzy.ai.LowerSectionStrategy;
import com.tauhka.games.yatzy.ai.UpperSectionStrategy;
import com.tauhka.games.yatzy.ai.YatzyAII;

/**
 * @author antsa-1 from GitHub 2 Jul 2022
 **/

public class YatzyAI extends YatzyPlayer implements AI, YatzyAII {

	private static final long serialVersionUID = 1L;

	public Hand deepContemplateHands(ScoreCard scoreCard, List<Dice> dices) {
		List<Dice> copies = deepCopyDices(dices);
		ContemplationResult lowerResult = new LowerSectionStrategy().deepContemplateHands(scoreCard, copies, this);
		Hand lowerSectionHand = lowerResult.getHand();
		if (lowerSectionHand != null && (lowerSectionHand.isStraight() || lowerSectionHand.isYatzy())) {
			return lowerSectionHand;
		}
		if (lowerSectionHand != null && (lowerSectionHand.isStraight() || lowerSectionHand.isYatzy())) {
			return lowerSectionHand;
		}
		if (lowerSectionHand != null && lowerSectionHand.isFullHouse()) {
			return lowerSectionHand;
		}

		ContemplationResult upperResult = new UpperSectionStrategy().deepContemplateHands(scoreCard, copies, this);
		Hand higherSectionHand = upperResult.getHand();
		if (higherSectionHand != null && higherSectionHand.isSixes()) {
			if (higherSectionHand.getValue() >= 18) {
				return higherSectionHand;
			}
		}
		if (higherSectionHand != null && higherSectionHand.isFives()) {
			if (higherSectionHand.getValue() >= 15) {
				return higherSectionHand;
			}
		}
		if (lowerSectionHand != null && lowerSectionHand.isFourOfKind()) {
			if (higherSectionHand != null && (higherSectionHand.isFives() || higherSectionHand.isSixes())) {
				return higherSectionHand;
			}
			return lowerSectionHand;
		}
		if (lowerSectionHand != null && higherSectionHand == null) {
			// Chance, Four of a kind,FullHouse, Three of a kind, Two pair, pair
			if (lowerSectionHand.isPairOrTrips()) {
				int upperSelectedCount = (int) upperResult.getCopies().stream().filter(dice -> dice.isSelected()).count();
				if (upperSelectedCount >= 3) {
					selectRealDices(copies, dices);
					return null;
				}
				return lowerSectionHand;
			}
		}
		if (lowerSectionHand == null && higherSectionHand != null) {
			int selected = (int) lowerResult.getCopies().stream().filter(dice -> dice.isSelected()).count();
			if (selected >= 3) {
				selectRealDices(lowerResult.getCopies(), dices);
				return null;
			}
			return higherSectionHand;
		}
		if (lowerSectionHand != null && getRollsLeft() == 0) {
			return lowerSectionHand;
		}
		if (higherSectionHand != null && getRollsLeft() == 0) {
			return higherSectionHand;
		}

		int lowerCount = (int) lowerResult.getCopies().stream().filter(dice -> dice.isSelected()).count();
		int higherCount = (int) upperResult.getCopies().stream().filter(dice -> dice.isSelected()).count();
		if (lowerCount > higherCount) {
			selectRealDices(lowerResult.getCopies(), dices);
		} else {
			selectRealDices(upperResult.getCopies(), dices);
		}
		// Dices are known, handType remains yet to be seen
		return new Hand(null, dices);
	}

	private void selectRealDices(List<Dice> copies, List<Dice> reals) {
		for (Dice d : reals) {
			int index = copies.indexOf(d);
			Dice copy = copies.get(index);
			if (copy.isSelected()) {
				d.select();
			}
		}
	}

	private List<Dice> deepCopyDices(List<Dice> dices) {

		List<Dice> copiedDices = new ArrayList<Dice>();
		for (Dice dice : dices) {
			Dice copy = new Dice(dice.getNumber());
			copy.setDiceId(dice.getDiceId());
			copiedDices.add(copy);
		}
		return copiedDices;
	}

}
