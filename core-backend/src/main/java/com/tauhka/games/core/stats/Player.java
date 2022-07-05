package com.tauhka.games.core.stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.tauhka.games.core.GameResultType;

/**
 * @author antsa-1 from GitHub 26 Jun 2022
 **/

public class Player {
	private UUID id;
	private String name;
	private Double initialRanking;
	private Double finalRanking; // average from rankingsAfter list
	private List<Double> rankingsAfter; // multiplayer tables compare rankings against each player
	private Status status;
	private Integer finishPosition;
	private int score; // yatzy game score

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

	public void setFinishPosition(Integer finishPosition) {
		this.finishPosition = finishPosition;
	}

	public Double getInitialRanking() {
		return initialRanking;
	}

	public int getScore() {
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

	public void setScore(int score) {
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

	public int getFinishPosition() {
		return finishPosition;
	}

	public void setFinishPosition(int finishPosition) {
		this.finishPosition = finishPosition;
	}

	public void setInitialRanking(double initialRanking) {
		this.initialRanking = initialRanking;
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

}
