package com.tauhka.games.yatzy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.tauhka.games.core.User;

/**
 * @author antsa-1 from GitHub 14 May 2022
 **/

public class YatzyPlayer extends User {
	private ScoreCard scoreCard;
	private List<Dice> dices = new ArrayList<Dice>();

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
