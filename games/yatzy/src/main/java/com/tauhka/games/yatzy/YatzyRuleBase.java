package com.tauhka.games.yatzy;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * @author antsa-1 from GitHub 28 May 2022
 **/

public class YatzyRuleBase {
	private static final int ALL_DICES_COUNT = 5;
	private static final Logger LOGGER = Logger.getLogger(YatzyRuleBase.class.getName());

	public void startGame(YatzyPlayer playerInTurn) {
		playerInTurn.removeDices();
		for (int i = 0; i < ALL_DICES_COUNT; i++) {
			Dice dice = new Dice();
			dice.roll();
			playerInTurn.addDice(dice);
		}
	}

	public YatzyTable playTurn(YatzyTable table, YatzyTurn turn) {
		if (turn.getYatzyAction() == YatzyAction.LOCK_DICES) {
			validateDices(table, turn.getDiceIds());
			lockDices(table.getPlayerInTurn(), turn.getDiceIds());
			return table;
		} else if (turn.getYatzyAction() == YatzyAction.ROLL_DICES) {
			rollUnlockDices(table.getPlayerInTurn());
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
			int index = table.getPlayerInTurn().getDices().indexOf(uuid);
			if (index == -1) {
				throw new IllegalArgumentException("No such dice " + id);
			}
		}
	}

	private List<Dice> rollUnlockDices(YatzyPlayer playerInTurn) {
		playerInTurn.rollUnlockedDices();
		return playerInTurn.getDices();
	}

	private List<Dice> lockDices(YatzyPlayer playerInTurn, List<String> diceIds) {
		playerInTurn.lockDices(diceIds);
		return playerInTurn.getDices();
	}

	private ScoreCard selectHand(YatzyPlayer playerInTurn, Hand hand) {
		ScoreCard card = playerInTurn.getScoreCard();
		card.setHand(hand.getHandType(), hand);
		return card;
	}
}
