package com.tauhka.games.yatzy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.tauhka.games.core.User;

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

	public List<Dice> rollUnlockedDices(YatzyTable table, List<Dice> diceIds, User user) {
		validate(table, diceIds, user);
		List<Dice> dicesRolled = new ArrayList<Dice>(5);
		for (int i = 0; i < ALL_DICES_COUNT; i++) {
			Dice dice = table.getDices().get(i);
			if (!dice.isLocked()) {
				dice.roll();
				dicesRolled.add(dice);
			} else {
				throw new IllegalArgumentException("Input contains dice which is already locked" + dice);
			}
		}
		int rollsLeft = table.getPlayerInTurn().getRollsLeft() - 1;
		table.getPlayerInTurn().setRollsLeft(rollsLeft);
		return dicesRolled;
	}

	private void validate(YatzyTable table, List<Dice> diceIds, User user) {
		validatePlayer(table, user);
		validateDices(table, diceIds);
		validateRollsCount(table);
	}

	private void validatePlayer(YatzyTable table, User user) {
		if (!table.getPlayerInTurn().equals(user)) {
			throw new IllegalArgumentException("User is not in turn:" + user);
		}

	}

	private void validateRollsCount(YatzyTable table) {
		if (table.getPlayerInTurn().getRollsLeft() <= 0) {
			throw new IllegalArgumentException("User has zero rolls left");
		}
	}

	public YatzyTable playTurn(YatzyTable table, YatzyTurn turn) {
		// Select hand
		return table;
	}

	private void validateDices(YatzyTable table, List<Dice> diceIds) {
		List<Dice> dices = diceIds;
		if (dices == null) {
			throw new IllegalArgumentException("Dices are missing");
		}
		if (dices.size() == 0) {
			throw new IllegalArgumentException("Dices are missing2");
		}
		if (dices.size() > 5) {
			throw new IllegalArgumentException("Too many dices");
		}
		for (int i = 0; i < dices.size(); i++) {
			String id = dices.get(i).getDiceId().toString();
			UUID uuid = UUID.fromString(id);
			if (!id.equals(uuid.toString())) {
				throw new IllegalArgumentException("dice id fail");
			}
			@SuppressWarnings("unlikely-arg-type")
			int index = table.getDices().indexOf(uuid); //TODO..
			if (index == -1) {
				throw new IllegalArgumentException("No such dice " + id);
			}
		}
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
