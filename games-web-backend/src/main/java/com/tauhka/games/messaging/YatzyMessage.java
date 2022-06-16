package com.tauhka.games.messaging;

import java.util.List;

import com.tauhka.games.yatzy.Dice;
import com.tauhka.games.yatzy.Hand;
import com.tauhka.games.yatzy.ScoreCard;

import jakarta.json.bind.annotation.JsonbProperty;

/**
 * @author antsa-1 from GitHub 8 Jun 2022
 **/

public class YatzyMessage {
	@JsonbProperty("dices")
	public List<Dice> dices;
	@JsonbProperty("handVal")
	public Integer handVal;
	@JsonbProperty("whoPlayed")
	private String whoPlayed;
	@JsonbProperty("scoreCard")
	private ScoreCard scoreCard;

	@JsonbProperty("hand")
	public Hand addedHand;

	public List<Dice> getDices() {
		return dices;
	}

	public void setDices(List<Dice> dices) {
		this.dices = dices;
	}

	public Integer getHandVal() {
		return handVal;
	}

	public void setHandVal(Integer handVal) {
		this.handVal = handVal;
	}

	public Hand getAddedHand() {
		return addedHand;
	}

	public void setAddedHand(Hand addedHand) {
		this.addedHand = addedHand;
	}

	public ScoreCard getScoreCard() {
		return scoreCard;
	}

	public void setScoreCard(ScoreCard scoreCard) {
		this.scoreCard = scoreCard;
	}

	public String getWhoPlayed() {
		return whoPlayed;
	}

	public void setWhoPlayed(String whoPlayed) {
		this.whoPlayed = whoPlayed;
	}

}
