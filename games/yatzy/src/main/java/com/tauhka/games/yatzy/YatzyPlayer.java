package com.tauhka.games.yatzy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.tauhka.games.core.User;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;

/**
 * @author antsa-1 from GitHub 14 May 2022
 **/

public class YatzyPlayer extends User implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(YatzyPlayer.class.getName());
	private static final int ALL_DICES_COUNT = 5;
	@JsonbProperty("scoreCard")
	private ScoreCard scoreCard;
	@JsonbProperty("dices")
	private List<Dice> dices = new ArrayList<Dice>(5);
	@JsonbProperty("rollsLeft")
	private int rollsLeft = 3;

	public List<Dice> rollUnlockedDices() {
		if (rollsLeft <= 0) {
			LOGGER.severe("User has no rolls left:" + this);
		}
		for (int i = 0; i < ALL_DICES_COUNT; i++) {
			Dice dice = dices.get(i);
			if (!dice.isLocked()) {
				dice.roll();
				dices.add(dice);
			}
		}
		rollsLeft--;
		return dices;
	}

	public void lockDices(List<String> diceIds) {
		for (String diceIdString : diceIds) {
			UUID diceId = UUID.fromString(diceIdString);
			Stream<Dice> stream = getUnlockedDices().stream();
			Optional<Dice> optional = stream.filter(unlockedDice -> unlockedDice.getDiceId().equals(diceId)).findFirst();
			Dice dice = optional.get();
			dice.lock();
		}
	}

	public void selectHand(Hand hand) {
		scoreCard.setHand(hand.getHandType(), hand);
	}

	public void removeDices() {
		dices.clear();
	}

	public ScoreCard getScoreCard() {
		return scoreCard;
	}

	public void setScoreCard(ScoreCard scoreCard) {
		this.scoreCard = scoreCard;
	}

	public List<Dice> getDices() {
		return dices;
	}

	public int getRollsLeft() {
		return rollsLeft;
	}

	public void setRollsLeft(int rollsLeft) {
		this.rollsLeft = rollsLeft;
	}

	@JsonbTransient
	public List<Dice> getUnlockedDices() {
		return dices.stream().filter(dice -> !dice.isLocked()).collect(Collectors.toList());
	}

	public void addDice(Dice dice) {
		dices.add(dice);
	}

	public void lockDice(Dice dice) {
		dices.get(dices.indexOf(dice)).lock();
	}

}
