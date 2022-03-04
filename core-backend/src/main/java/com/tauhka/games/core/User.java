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

	@JsonbProperty
	private GameToken gameToken;
	@JsonbTransient
	private UUID id;
	@JsonbTransient
	private Double initialCalculationsRank = -1d;

	public String getName() {
		return name;
	}

	public User() {
		super();
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
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

	public Double getInitialCalculationsRank() {
		return initialCalculationsRank;
	}

	public void setInitialCalculationsRank(Double rankingTemp) {
		this.initialCalculationsRank = rankingTemp;
	}

	@Override
	public String toString() {
		return "User [name=" + name + "]";
	}

}
