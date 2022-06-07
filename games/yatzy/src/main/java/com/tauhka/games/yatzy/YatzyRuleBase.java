package com.tauhka.games.yatzy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * @author antsa-1 from GitHub 28 May 2022
 **/

public class YatzyRuleBase {
	private static final int ALL_DICES_COUNT = 5;
	private static final Logger LOGGER = Logger.getLogger(YatzyRuleBase.class.getName());

	public void startGame(YatzyTable yatzyTable) {
		if (yatzyTable.getDices() == null) {
			yatzyTable.setDices(new ArrayList<Dice>(5));
		}
		for (int i = 0; i < ALL_DICES_COUNT; i++) {
			Dice dice = new Dice();
			dice.roll();
			yatzyTable.getDices().add(dice);
		}
	}

	public YatzyTable playTurn(YatzyTable table, YatzyTurn turn) {
		if (turn.getYatzyAction() == YatzyAction.LOCK_DICES) {
			validateDices(table, turn.getDiceIds());
			lockDices(table, turn.getDiceIds());
			return table;
		} else if (turn.getYatzyAction() == YatzyAction.ROLL_DICES) {
			rollUnlockedDices(table);
			return table;
		} else if (turn.getYatzyAction() == YatzyAction.SELECT_HAND) {
			return table;
		}
		throw new IllegalArgumentException("Wrong turn parameter" + turn);
	}

	private void validateDices(YatzyTable table, List<String> dices) {
		if (dices == null) {
			throw new IllegalArgumentException("Dices are missing");
		}
		if (dices.size() == 0) {
			throw new IllegalArgumentException("Dices are missing2");
		}
		if (dices.size() > 5) {
			throw new IllegalArgumentException("Two many dices");
		}
		for (int i = 0; i < dices.size(); i++) {
			String id = dices.get(i);
			UUID uuid = UUID.fromString(id);
			if (!id.equals(uuid.toString())) {
				throw new IllegalArgumentException("dice id fail");
			}
			@SuppressWarnings("unlikely-arg-type")
			int index = table.getDices().indexOf(uuid);
			if (index == -1) {
				throw new IllegalArgumentException("No such dice " + id);
			}
		}
	}

	public void rollUnlockedDices(YatzyTable table) {
		if (table.getPlayerInTurn().getRollsLeft() <= 0) {
			LOGGER.severe("User has no rolls left:" + this);
			throw new IllegalArgumentException("No rolls left");
		}
		for (int i = 0; i < ALL_DICES_COUNT; i++) {
			Dice dice = table.getDices().get(i);
			if (!dice.isLocked()) {
				dice.roll();
			}
		}
		int rollsLeft = table.getPlayerInTurn().getRollsLeft() - 1;
		table.getPlayerInTurn().setRollsLeft(rollsLeft);
	}

	public void lockDices(YatzyTable table, List<String> diceIds) {
		for (String diceIdString : diceIds) {
			UUID diceId = UUID.fromString(diceIdString);
			Stream<Dice> stream = table.getUnlockedDices().stream();
			Optional<Dice> optional = stream.filter(unlockedDice -> unlockedDice.getDiceId().equals(diceId)).findFirst();
			Dice dice = optional.get();
			dice.lock();
		}
	}




	private ScoreCard selectHand(YatzyPlayer playerInTurn, Hand hand) {
		ScoreCard card = playerInTurn.getScoreCard();
		card.setHand(hand.getHandType(), hand);
		return card;
	}
}
