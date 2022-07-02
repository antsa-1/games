package com.tauhka.games.messaging;

import java.util.List;

import com.tauhka.games.core.GameResultType;
import com.tauhka.games.yatzy.Dice;
import com.tauhka.games.yatzy.Hand;
import com.tauhka.games.yatzy.ScoreCard;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;

/**
 * @author antsa-1 from GitHub 8 Jun 2022
 **/

public class YatzyMessage {
	@JsonbProperty("dices")
	public List<Dice> dices;
	@JsonbProperty("handType")
	public Integer handType;
	@JsonbProperty("whoPlayed")
	private String whoPlayed;
	@JsonbProperty("scoreCard")
	private ScoreCard scoreCard;

	@JsonbProperty("gameOver")
	public boolean gameOver;

	@JsonbProperty("result")
	public GameResultType result;

	@JsonbTransient
	private Hand hand;

	public List<Dice> getDices() {
		return dices;
	}

	public void setDices(List<Dice> dices) {
		this.dices = dices;
	}

	public ScoreCard getScoreCard() {
		return scoreCard;
	}

	public Integer getHandType() {
		return handType;
	}

	public void setHandType(Integer handType) {
		this.handType = handType;
	}

	public boolean isGameOver() {
		return gameOver;
	}

	public Hand getHand() {
		return hand;
	}

	public void setHand(Hand hand) {
		this.hand = hand;
	}

	public void setGameOver(boolean gameOver) {
		this.gameOver = gameOver;
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

	public GameResultType getResult() {
		return result;
	}

	public void setResult(GameResultType result) {
		this.result = result;
	}

}
