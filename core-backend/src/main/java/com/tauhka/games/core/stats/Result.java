package com.tauhka.games.core.stats;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
		return players.stream().filter(player -> player.getId().equals(id)).findFirst().get();
	}

	public Player findPlayer(String nickname) {
		Stream<Player> stream = players.stream();
		Optional<Player> playerOptional = stream.filter(player -> player.getName().equals(nickname)).findFirst();
		return playerOptional.get();
	}

	public void changeStatus(Player p, GameResultType type) {

	}

	public Instant getStartInstant() {
		return startInstant;
	}

	public void setStartInstant(Instant startInstant) {
		this.startInstant = startInstant;
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

	public void changeStatus(String name, UUID id, Status status) {
		if (id != null) {
			findPlayer(id).setStatus(status);
			return;
		}
		findPlayer(name).setStatus(status);
	}
}
