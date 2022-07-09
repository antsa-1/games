package com.tauhka.games.yatzy;

import java.io.Serializable;
import java.util.logging.Logger;

import com.tauhka.games.core.User;
import com.tauhka.games.core.util.Constants;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;

/**
 * @author antsa-1 from GitHub 14 May 2022
 **/

public class YatzyPlayer extends User implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(YatzyPlayer.class.getName());
	@JsonbProperty("scoreCard")
	private ScoreCard scoreCard;
	@JsonbProperty("rollsLeft")
	private int rollsLeft = 3;
	@JsonbProperty("enabled")
	private boolean enabled;

	public YatzyPlayer() {
		scoreCard = new ScoreCard();
		enabled = true;
	}

	@JsonbTransient
	public boolean isComputerPlayer() {
		return this.getId() != null && this.getId().equals(Constants.OLAV_COMPUTER_UUID);
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

	public boolean isEnabled() {
		return enabled;
	}

	public boolean hasRollsLeft() {
		return rollsLeft > 0;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String toString() {
		return "YatzyPlayer [scoreCard=" + scoreCard + ", rollsLeft=" + rollsLeft + ", enabled=" + enabled + ", getName()=" + getName() + "]";
	}

}
