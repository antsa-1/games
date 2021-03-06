package com.tauhka.games.messaging;

import java.io.Serializable;

import com.tauhka.games.core.User;
import com.tauhka.games.core.Vector2d;
import com.tauhka.games.pool.Cue;
import com.tauhka.games.pool.CueBall;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;

/** @author antsa-1 from GitHub 26 Mar 2022 **/

public class PoolMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	@JsonbProperty("cue")
	private Cue cue;
	@JsonbProperty("cueBall")
	private CueBall cueBall;
	@JsonbTransient
	private Vector2d canvas;
	@JsonbProperty("turnResult")
	private String turnResult;
	@JsonbProperty("winner")
	private User winner;
	@JsonbProperty("selectedPocket")
	private int selectedPocket;
	@JsonbProperty("whoPlayed")
	private String whoPlayed;

	public Cue getCue() {
		return cue;
	}

	public void setCue(Cue cue) {
		this.cue = cue;
	}

	public CueBall getCueBall() {
		return cueBall;
	}

	public void setCueBall(CueBall cueBall) {
		this.cueBall = cueBall;
	}

	public Vector2d getCanvas() {
		return canvas;
	}

	public void setCanvas(Vector2d canvas) {
		this.canvas = canvas;
	}

	public String getTurnResult() {
		return turnResult;
	}

	public void setTurnResult(String turnResult) {
		this.turnResult = turnResult;
	}

	public User getWinner() {
		return winner;
	}

	public String getWhoPlayed() {
		return whoPlayed;
	}

	public void setWhoPlayed(String whoPlayed) {
		this.whoPlayed = whoPlayed;
	}

	public void setWinner(User winner) {
		this.winner = winner;
	}

	public int getSelectedPocket() {
		return selectedPocket;
	}

	public void setSelectedPocket(int selectedPocket) {
		this.selectedPocket = selectedPocket;
	}

}
