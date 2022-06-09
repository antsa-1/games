package com.tauhka.games.yatzy;

import java.io.Serializable;
import java.util.logging.Logger;

import com.tauhka.games.core.User;

import jakarta.json.bind.annotation.JsonbProperty;

/**
 * @author antsa-1 from GitHub 14 May 2022
 **/

public class YatzyPlayer extends User implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(YatzyPlayer.class.getName());
	private static final int ALL_DICES_COUNT = 5;
	@JsonbProperty("scoreCard")
	private ScoreCard scoreCard;
	@JsonbProperty("rollsLeft")
	private int rollsLeft = 3;

	public void selectHand(Hand hand) {
		scoreCard.setHand(hand.getHandType(), hand);
	}

	public ScoreCard getScoreCard() {
		return scoreCard;
	}

	public void setScoreCard(ScoreCard scoreCard) {
		this.scoreCard = scoreCard;
	}

	public int getRollsLeft() {
		return rollsLeft;
	}

	public void setRollsLeft(int rollsLeft) {
		this.rollsLeft = rollsLeft;
	}

}
