package com.tauhka.games.core.stats;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.GameResultType;
import com.tauhka.games.core.timer.TimeControlIndex;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;

/**
 * @author antsa-1 from GitHub 26 Jun 2022
 **/

public class Result {
	@JsonbProperty("players")
	private List<Player> players = new ArrayList<Player>();
	@JsonbTransient
	private Instant endInstant = Instant.now();
	@JsonbTransient
	private Instant startInstant;
	@JsonbTransient
	private UUID gameId;
	@JsonbTransient
	private GameMode gameMode;
	@JsonbTransient
	private TimeControlIndex timeControlIndex;
	@JsonbTransient
	private boolean complete;

	public Instant getEndInstant() {
		return endInstant;
	}

	public void addPlayer(Player p) {
		players.add(p);
	}

	public void setEndInstant(Instant endInstant) {
		this.endInstant = endInstant;
	}

	public Player findPlayer(UUID id) {
		return players.stream().filter(player -> player.getId() != null && player.getId().equals(id)).findFirst().get();
	}

	public Player findPlayer(String nickname) {
		Stream<Player> stream = players.stream();
		Optional<Player> playerOptional = stream.filter(player -> player.getName().equals(nickname)).findFirst();
		return playerOptional.get();
	}

	public void changeStatus(Player p, GameResultType type) {

	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	public Instant getStartInstant() {
		return startInstant;
	}

	public void setStartInstant(Instant startInstant) {
		this.startInstant = startInstant;
	}

	public TimeControlIndex getTimeControlIndex() {
		return timeControlIndex;
	}

	public void setTimeControlIndex(TimeControlIndex timeControlIndex) {
		this.timeControlIndex = timeControlIndex;
	}

	public UUID getGameId() {
		return gameId;
	}

	public void setGameId(UUID gameId) {
		this.gameId = gameId;
	}

	public GameMode getGameMode() {
		return gameMode;
	}

	public void setGameMode(GameMode gameMode) {
		this.gameMode = gameMode;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public List<Player> getPlayersWithInitialRanking() {
		return players.stream().filter(player -> player.getId() != null && player.getInitialRanking() != null).collect(Collectors.toList());
	}

	public List<Player> getOtherRankedPlayers(UUID myId) {
		return players.stream().filter(player -> player.getId() != null && !player.getId().equals(myId)).collect(Collectors.toList());
	}

	public void changeStatus(String name, UUID id, Status status) {
		if (id != null) {
			findPlayer(id).setStatus(status);
			return;
		}
		findPlayer(name).setStatus(status);
	}

	@Override
	public String toString() {
		return "Result [players=" + players + ", endInstant=" + endInstant + ", startInstant=" + startInstant + ", gameId=" + gameId + ", gameMode=" + gameMode + ", timeControlIndex=" + timeControlIndex + ", complete=" + complete + "]";
	}

}
