package com.tauhka.games.core;

import java.util.Objects;
import java.util.UUID;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;

public class User { // Combine with Portal User.java?
	@JsonbProperty("name")
	private String name;
	@JsonbProperty("ranking")
	private int ranking = 0;
	@JsonbProperty("wins")
	private int wins = 0;
	@JsonbProperty("draws")
	private int draws = 0;
	@JsonbProperty
	private GameToken gameToken;

	@JsonbTransient
	private UUID id;

	public String getName() {
		return name;
	}

	public User() {
		super();
	}

	public void addWin() {
		this.wins++;
	}

	public void addDraw() {
		this.draws++;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public int getWins() {
		return wins;
	}

	public void setWins(int wins) {
		this.wins = wins;
	}

	public void setName(String name) {
		this.name = name;
	}

	public User(String name) {
		super();
		this.name = name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	public int getRanking() {
		return ranking;
	}

	public void setRanking(int ranking) {
		this.ranking = ranking;
	}

	public int getDraws() {
		return draws;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		return Objects.equals(name, other.name);
	}

	public GameToken getGameToken() {
		return gameToken;
	}

	public void setGameToken(GameToken gameToken) {
		this.gameToken = gameToken;
	}

	@Override
	public String toString() {
		return "User [name=" + name + ", ranking=" + ranking + "]";
	}

}
