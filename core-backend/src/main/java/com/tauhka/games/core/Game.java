package com.tauhka.games.core;

import java.util.List;

import jakarta.json.bind.annotation.JsonbProperty;

/**
 * @author antsa-1 from GitHub 25 Mar 2022
 **/

public class Game {
	@JsonbProperty("gameId")
	private int gameId;
	@JsonbProperty("name")
	private String name;
	@JsonbProperty("gameModes")
	private List<GameMode> gameModes;

	public int getGameId() {
		return gameId;
	}

	public void setGameId(int gameId) {
		this.gameId = gameId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<GameMode> getGameModes() {
		return gameModes;
	}

	public void setGameModes(List<GameMode> gameModes) {
		this.gameModes = gameModes;
	}

}
