package com.tauhka.portal.profile;

import java.time.Instant;
import java.util.List;

import com.tauhka.games.core.stats.Player;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.persistence.Convert;

/**
 * @author antsa-1 from GitHub 12 Jul 2022
 **/

public class Game {
	@JsonbProperty("gameId")
	private String gameId;

	@JsonbProperty("gameType")
	private Integer gameType;

	private List<Player> players;

	@Convert(converter = InstantConverter.class)
	@JsonbProperty("startInstant")
	private Instant startInstant;

	@Convert(converter = InstantConverter.class)
	@JsonbProperty("endInstant")
	private Instant endInstant;

	@JsonbProperty("winner")
	private String winner;

	@JsonbProperty("result")
	private Integer result;

	@JsonbProperty("description")
	private String description;
	@JsonbProperty("finishStatus")
	private String finishStatus;

	@JsonbProperty("timeControlSeconds")
	private int timeControlSeconds;

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getGameType() {
		return gameType;
	}

	public void setGameType(Integer gameType) {
		this.gameType = gameType;
	}

	public List<com.tauhka.games.core.stats.Player> getPlayers() {
		return players;
	}

	public void setPlayers(List<com.tauhka.games.core.stats.Player> players) {
		this.players = players;
	}

	public String getFinishStatus() {
		return finishStatus;
	}

	public void setFinishStatus(String finishStatus) {
		this.finishStatus = finishStatus;
	}

	public int getTimeControlSeconds() {
		return timeControlSeconds;
	}

	public void setTimeControlSeconds(int timeControlSeconds) {
		this.timeControlSeconds = timeControlSeconds;
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

	public String getWinner() {
		return winner;
	}

	public void setWinner(String winner) {
		this.winner = winner;
	}

	public Integer getResult() {
		return result;
	}

	public void setResult(Integer result) {
		this.result = result;
	}

	public void setEndInstant(Instant endInstant) {
		this.endInstant = endInstant;
	}

}
