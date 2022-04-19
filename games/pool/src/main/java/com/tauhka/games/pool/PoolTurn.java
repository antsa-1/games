package com.tauhka.games.pool;

import com.tauhka.games.core.User;
import com.tauhka.games.core.Vector2d;

import jakarta.json.bind.annotation.JsonbProperty;

/**
 * @author antsa-1 from GitHub 25 Mar 2022
 **/

public class PoolTurn {
	@JsonbProperty("cueBall")
	private CueBall cueBall;
	@JsonbProperty("cue")
	private Cue cue;
	@JsonbProperty("canvas")
	private Vector2d canvas;

	@JsonbProperty("handBall")
	private Vector2d handBall;

	@JsonbProperty("turnResult")
	private TurnResult turnResult;
	@JsonbProperty("winner")
	private User winner;
	@JsonbProperty("selectedPocket")
	private Integer selectedPocket;
	@JsonbProperty("player")
	private String player;
	@JsonbProperty("turnType")
	private TurnType turnType;

	public CueBall getCueBall() {
		return cueBall;
	}

	public void setCueBall(CueBall cueBall) {
		this.cueBall = cueBall;
	}

	public Cue getCue() {
		return cue;
	}

	public void setCue(Cue cue) {
		this.cue = cue;
	}

	public Vector2d getCanvas() {
		return canvas;
	}

	public TurnType getTurnType() {
		return turnType;
	}

	public void setTurnType(TurnType turnType) {
		this.turnType = turnType;
	}

	public void setCanvas(Vector2d canvas) {
		this.canvas = canvas;
	}

	public String getPlayer() {
		return player;
	}

	public void setPlayer(String player) {
		this.player = player;
	}

	public Vector2d getHandBall() {
		return handBall;
	}

	public void setHandBall(Vector2d handBall) {
		this.handBall = handBall;
	}

	public TurnResult getTurnResult() {
		return turnResult;
	}

	public void setTurnResult(TurnResult turnResult) {
		this.turnResult = turnResult;
	}

	public User getWinner() {
		return winner;
	}

	public void setWinner(User winner) {
		this.winner = winner;
	}

	public Integer getSelectedPocket() {
		return selectedPocket;
	}

	public void setSelectedPocket(Integer selectedPocket) {
		this.selectedPocket = selectedPocket;
	}

}
