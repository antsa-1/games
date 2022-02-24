package com.tauhka.games.core.twodimen;

import java.time.Instant;
import java.util.UUID;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.GameToken;
import com.tauhka.games.core.User;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;

public class GameResult {
	@JsonbProperty("fromX") // WinRow starts coordinates
	private int fromX;
	@JsonbProperty("fromY")
	private int fromY;
	@JsonbProperty("toX") // WinRow ends coordinates
	private int toX;
	@JsonbProperty("toY")
	private int toY;
	@JsonbProperty("token")
	private GameToken token;

	@JsonbProperty("winner") // Winner
	private User winner;

	@JsonbProperty
	private GameResultType resultType;
	@JsonbTransient
	private User playerA;
	@JsonbTransient
	private User playerB;
	@JsonbTransient
	private Instant endInstant = Instant.now();
	@JsonbTransient
	private Instant startInstant;
	@JsonbTransient
	private UUID gameId;
	@JsonbTransient
	private GameMode gameMode;

	public GameResult() {
		this.gameId = UUID.randomUUID();
	}

	public int getFromX() {
		return fromX;
	}

	public int getFromY() {
		return fromY;
	}

	public int getToX() {
		return toX;
	}

	public int getToY() {
		return toY;
	}

	public GameToken getToken() {
		return token;
	}

	public void setFromX(int fromX) {
		this.fromX = fromX;
	}

	public void setFromY(int fromY) {
		this.fromY = fromY;
	}

	public UUID getGameId() {
		return gameId;
	}

	public Instant getStartInstant() {
		return startInstant;
	}

	public void setStartInstant(Instant startInstant) {
		this.startInstant = startInstant;
	}

	public Instant getEndInstant() {
		return endInstant;
	}

	public void setToX(int toX) {
		this.toX = toX;
	}

	public void setToY(int toY) {
		this.toY = toY;
	}

	public void setToken(GameToken token) {
		this.token = token;
	}

	public User getPlayerA() {
		return playerA;
	}

	public void setPlayerA(User playerA) {
		this.playerA = playerA;
	}

	public User getPlayerB() {
		return playerB;
	}

	public void setPlayerB(User playerB) {
		this.playerB = playerB;
	}

	public GameResultType getResultType() {
		return resultType;
	}

	public void setResultType(GameResultType resultType) {
		this.resultType = resultType;
	}

	public GameMode getGameMode() {
		return gameMode;
	}

	public void setGameMode(GameMode gameMode) {
		this.gameMode = gameMode;
	}

	public User getWinner() {
		return winner;
	}

	public void setWinner(User winner) {
		this.winner = winner;
	}

	@Override
	public String toString() {
		return "GameResult [fromX=" + fromX + ", fromY=" + fromY + ", toX=" + toX + ", toY=" + toY + ", token=" + token + ", winner=" + winner + ", resultType=" + resultType + ", playerA=" + playerA + ", playerB=" + playerB
				+ ", endInstant=" + endInstant + ", startInstant=" + startInstant + ", gameId=" + gameId + ", gameMode=" + gameMode + "]";
	}

}
