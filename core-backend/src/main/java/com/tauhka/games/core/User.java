package com.tauhka.games.core;

import java.util.Objects;
import java.util.UUID;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;

public class User { // Combine with Portal User.java?
	@JsonbProperty("name")
	private String name;
	@JsonbProperty("rankingTictactoe")
	private Double rankingTictactoe = 0d;
	@JsonbProperty("rankingConnectFour")
	private Double rankingConnectFour = 0d;
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

	public Double getRankingTictactoe() {
		return rankingTictactoe;
	}

	public void setRankingTictactoe(Double rankingTictactoe) {
		this.rankingTictactoe = rankingTictactoe;
	}

	public Double getRankingConnectFour() {
		return rankingConnectFour;
	}

	public void setRankingConnectFour(Double rankingConnectFour) {
		this.rankingConnectFour = rankingConnectFour;
	}

	public User(String name) {
		super();
		this.name = name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
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
		return "User [name=" + name + ", rankingTictactoe=" + rankingTictactoe + ", rankingConnectFour=" + rankingConnectFour + ", wins=" + wins + ", draws=" + draws + ", gameToken=" + gameToken + ", id=" + id + "]";
	}

}
