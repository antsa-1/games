package com.tauhka.games.messaging;

import java.util.List;

import com.tauhka.games.core.GameResultType;
import com.tauhka.games.yatzy.Dice;
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

	@JsonbProperty("gameOver")
	public boolean gameOver;

	@JsonbProperty("result")
	public GameResultType result;

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

	public ScoreCard getScoreCard() {
		return scoreCard;
	}

	public boolean isGameOver() {
		return gameOver;
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
