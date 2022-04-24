package com.tauhka.games.core.twodimen;

import java.time.Instant;
import java.util.UUID;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.GameResultType;
import com.tauhka.games.core.User;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;

public class GameResult {

	@JsonbProperty("winner") // Winner
	private User winner;
	@JsonbProperty("resigner") // Winner
	private User resigner;

	@JsonbProperty("resultType")
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

	public User getResigner() {
		return resigner;
	}

	public void setResigner(User resigner) {
		this.resigner = resigner;
	}

}
