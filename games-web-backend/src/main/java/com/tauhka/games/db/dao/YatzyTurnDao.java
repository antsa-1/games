package com.tauhka.games.db.dao;

import java.util.UUID;

/**
 * @author antsa-1 from GitHub 28 Jun 2022
 **/

public class YatzyTurnDao {

	private String playerName;
	private String playerId;
	private Integer dice1Value;
	private Integer dice2Value;
	private Integer dice3Value;
	private Integer dice4Value;
	private Integer dice5Value;
	private Integer score;
	private String handType;
	private String gameId;

	public YatzyTurnDao(String playerName, UUID playerId, Integer dice1, Integer dice2, Integer dice3, Integer dice4, Integer dice5, Integer score, String handType, UUID gameId) {
		super();
		this.playerName = playerName;
		this.playerId = playerId != null ? playerId.toString() : "";
		this.dice1Value = dice1;
		this.dice2Value = dice2;
		this.dice3Value = dice3;
		this.dice4Value = dice4;
		this.dice5Value = dice5;
		this.score = score;
		this.handType = handType;
		this.gameId = gameId.toString();
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public Integer getDice1Value() {
		return dice1Value;
	}

	public void setDice1Value(Integer dice1Value) {
		this.dice1Value = dice1Value;
	}

	public Integer getDice2Value() {
		return dice2Value;
	}

	public void setDice2Value(Integer dice2Value) {
		this.dice2Value = dice2Value;
	}

	public Integer getDice3Value() {
		return dice3Value;
	}

	public void setDice3Value(Integer dice3Value) {
		this.dice3Value = dice3Value;
	}

	public Integer getDice4Value() {
		return dice4Value;
	}

	public void setDice4Value(Integer dice4Value) {
		this.dice4Value = dice4Value;
	}

	public Integer getDice5Value() {
		return dice5Value;
	}

	public void setDice5Value(Integer dice5Value) {
		this.dice5Value = dice5Value;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public String getHandType() {
		return handType;
	}

	public void setHandType(String handType) {
		this.handType = handType;
	}

	@Override
	public String toString() {
		return "YatzyTurnDao [playerName=" + playerName + ", playerId=" + playerId + ", dice1Value=" + dice1Value + ", dice2Value=" + dice2Value + ", dice3Value=" + dice3Value + ", dice4Value=" + dice4Value + ", dice5Value=" + dice5Value
				+ ", score=" + score + ", handType=" + handType + ", gameId=" + gameId + "]";
	}

}
