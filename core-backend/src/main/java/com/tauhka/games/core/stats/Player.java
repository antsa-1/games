package com.tauhka.games.core.stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.tauhka.games.core.util.Constants;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;

/**
 * @author antsa-1 from GitHub 26 Jun 2022
 **/

public class Player implements Comparable<Player> {
	@JsonbTransient
	private UUID id;
	@JsonbProperty(value = "name")
	private String name;
	@JsonbProperty(value = "initialRanking")
	private Double initialRanking;
	@JsonbProperty(value = "finalRanking")
	private Double finalRanking; // average from rankingsAfter list
	@JsonbTransient
	private List<Double> rankingsAfter; // multiplayer tables compare rankings against each player
	@JsonbTransient
	private Status status;
	@JsonbProperty(value = "finishPosition")
	private Integer finishPosition;
	@JsonbProperty(value = "score")
	private Integer score; // yatzy game score

	@JsonbTransient
	public boolean isComputerPlayer() {
		return this.id != null && this.id.equals(Constants.OLAV_COMPUTER_UUID);
	}

	public Player() {
	}

	public Player(UUID id) {
		super();
		this.id = id;
		rankingsAfter = new ArrayList<Double>();
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isGuest() {
		return this.name.startsWith(Constants.GUEST_LOGIN_NAME);
	}

	public void setFinishPosition(Integer finishPosition) {
		this.finishPosition = finishPosition;
	}

	public Double getInitialRanking() {
		return initialRanking;
	}

	public Integer getScore() {
		return score;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Player other = (Player) obj;
		return Objects.equals(id, other.id);
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Double getFinalRanking() {
		return finalRanking;
	}

	public void setFinalRanking(Double finalRanking) {
		this.finalRanking = finalRanking;
	}

	public void setInitialRanking(Double initialRanking) {
		this.initialRanking = initialRanking;
	}

	public Integer getFinishPosition() {
		return finishPosition;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public List<Double> getRankingsAfter() {
		return rankingsAfter;
	}

	public void setRankingsAfter(List<Double> rankingsAfter) {
		this.rankingsAfter = rankingsAfter;
	}

	@Override
	public int compareTo(Player other) {
		// Any of the players might not have played any hand
		if (this.score == null) {
			return other.score == null ? 0 : 1;
		}
		if (other.score == null) {
			return this.score == null ? 0 : -1;
		}
		int otherScore = other.score;
		if (score > otherScore) {
			return -1;
		} else if (otherScore > score) {
			return 1;
		}
		return 0;
	}

}
