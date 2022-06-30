package com.tauhka.games.yatzy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.tauhka.games.core.User;
import com.tauhka.games.core.stats.Player;
import com.tauhka.games.core.stats.Result;

/**
 * @author antsa-1 from GitHub 28 May 2022
 **/

public class YatzyRuleBase {
	private static final int ALL_DICES_COUNT = 5;
	private static final Logger LOGGER = Logger.getLogger(YatzyRuleBase.class.getName());

	public void startGame(YatzyTable yatzyTable) {
		yatzyTable.setStartTime(Instant.now());
		if (yatzyTable.getRandomizeStarter()) {
			int i = ThreadLocalRandom.current().nextInt(1, yatzyTable.getPlayers().size());
			yatzyTable.setStartingPlayer(yatzyTable.getPlayers().get(i - 1));
			yatzyTable.setPlayerInTurn(yatzyTable.getStartingPlayer());
		} else {
			yatzyTable.setStartingPlayer(yatzyTable.getPlayers().get(0));
			yatzyTable.setPlayerInTurn(yatzyTable.getStartingPlayer());
		}
		yatzyTable.getPlayerInTurn().setRollsLeft(3);
		yatzyTable.setDices(new ArrayList<Dice>(5));
		for (int i = 0; i < ALL_DICES_COUNT; i++) {
			Dice dice = new Dice();
			dice.setDiceId(UUID.randomUUID());
			dice.roll();
			yatzyTable.getDices().add(dice);
		}
		yatzyTable.setGameResult(new Result());
		for (YatzyPlayer y : yatzyTable.getPlayers()) {
			// Players will now be in result of the game regardless of how the game ends
			Player p = new Player(y.getId());
			p.setName(y.getName());
			yatzyTable.getGameResult().addPlayer(p);
			y.setScoreCard(new ScoreCard());
			y.setEnabled(true);
		}
		yatzyTable.setGameId(UUID.randomUUID());
		yatzyTable.startTimer();
	}

	public List<Dice> rollDices(YatzyTable table, List<Dice> dicesToBeRolled, User user) {
		validate(table, dicesToBeRolled, user);
		List<Dice> dicesRolled = new ArrayList<Dice>(5);
		for (int i = 0; i < dicesToBeRolled.size(); i++) {
			Stream<Dice> stream = table.getDices().stream();
			final int index = i;
			Optional<Dice> diceOptional = stream.filter(dice -> dice.equals(dicesToBeRolled.get(index)) && !dice.isSelected()).findFirst();
			if (!diceOptional.isPresent()) {
				throw new IllegalArgumentException("Something went wrong, no dice found:" + table);
			}
			Dice d = diceOptional.get();
			d.roll();
			dicesRolled.add(d);
		}

		int rollsLeft = table.getPlayerInTurn().getRollsLeft() - 1;
		table.getPlayerInTurn().setRollsLeft(rollsLeft);
		return dicesRolled;
	}

	private void validate(YatzyTable table, List<Dice> diceIds, User user) {
		validatePlayer(table, user);
		validateIncomingDices(table, diceIds);
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

	private void validateIncomingDices(YatzyTable table, List<Dice> incomingDices) {
		if (incomingDices == null) {
			throw new IllegalArgumentException("Dices are missing");
		}
		if (incomingDices.size() == 0) {
			throw new IllegalArgumentException("Dices are missing2");
		}
		if (incomingDices.size() > 5) {
			throw new IllegalArgumentException("Too many dices");
		}
		for (int i = 0; i < incomingDices.size(); i++) {
			Dice incomingDice = incomingDices.get(i);

			int index = table.getDices().indexOf(incomingDice);
			if (index == -1) {
				throw new IllegalArgumentException("No such dice " + incomingDice);
			}
		}
	}

	public ScoreCard selectHand(YatzyTable table, User user, Integer handType) {
		Hand hand = new Hand(HandType.getHandType(handType), table.getDices());
		ScoreCard scoreCard = table.getPlayerInTurn().getScoreCard();
		scoreCard.addHand(hand);
		return scoreCard;
	}
}
