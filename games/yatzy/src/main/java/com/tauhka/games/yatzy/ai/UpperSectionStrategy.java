package com.tauhka.games.yatzy.ai;

import java.util.List;

import com.tauhka.games.yatzy.Dice;
import com.tauhka.games.yatzy.Hand;
import com.tauhka.games.yatzy.HandType;
import com.tauhka.games.yatzy.ScoreCard;
import com.tauhka.games.yatzy.YatzyAI;
import com.tauhka.games.yatzy.util.HandCalculator;

/**
 * @author antsa-1 from GitHub 4 Jul 2022
 **/

public class UpperSectionStrategy {
	public ContemplationResult deepContemplateHands(ScoreCard scoreCard, List<Dice> dices, YatzyAI yatzyComputer) {
		ContemplationResult result = new ContemplationResult();
		result.setCopies(dices);
		Hand h = new Hand(dices);
		int number = -1;

		for (int i = 5; i >= 1; i--) {
			number = HandCalculator.getNumberWhichExistAtLeastTimes(i, h);
			if (number < 0) {
				continue;
			}
			HandType handType = HandType.getHandType(number);
			if (scoreCard.isMissingHandType(handType)) {
				if (number > 0 && i == 5) {
					h.setHandType(HandType.getHandType(number)); // 5 same number -> don't roll even if rolls left
					result.setHand(h);
					return result;
				} else if (number > 0 && yatzyComputer.hasRollsLeft()) {
					yatzyComputer.selectNumbers(dices, number);
					result.setHand(h);
					return result;
				} else if (number > 0) {
					h.setHandType(HandType.getHandType(number));
					result.setHand(h);
					return result;
				}
			}
		}
		// No hand, no Dice selections
		return result;
	}
}
