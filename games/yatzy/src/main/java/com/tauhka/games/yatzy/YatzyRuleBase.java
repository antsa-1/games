package com.tauhka.games.yatzy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * @author antsa-1 from GitHub 28 May 2022
 **/

public class YatzyRuleBase {
	private static final int ALL_DICES_COUNT = 5;

	public void startTurn(YatzyPlayer playerInTurn) {
		playerInTurn.removeDices();
		for (int i = 0; i < ALL_DICES_COUNT; i++) {
			Dice dice = new Dice();
			dice.roll();
			playerInTurn.addDice(dice);
		}
	}

	public List<Dice> throwUnlockedDices(YatzyPlayer playerInTurn) {
		List<Dice> dices = new ArrayList<Dice>(5);
		for (int i = 0; i < ALL_DICES_COUNT; i++) {
			Dice dice = playerInTurn.getDices().get(i);
			if (!dice.isLocked()) {
				dice.roll();
				dices.add(dice);
			}
		}
		return dices;
	}

	public void lockDices(YatzyPlayer playerInTurn, List<String> diceIds) {
		for (String diceIdString : diceIds) {
			UUID diceId = UUID.fromString(diceIdString);
			Stream<Dice> stream = playerInTurn.getUnlockedDices().stream();
			Optional<Dice> optional = stream.filter(unlockedDice -> unlockedDice.getDiceId().equals(diceId)).findFirst();
			Dice dice = optional.get();
			dice.lock();
		}
	}

	public void selectHand(YatzyPlayer playerInTurn, Hand hand) {
		ScoreCard card = playerInTurn.getScoreCard();
		card.setHand(hand.getHandType(), hand);
	}
}
