package com.tauhka.games.core.twodimen;

import com.tauhka.games.core.GameToken;

import jakarta.json.bind.annotation.JsonbProperty;

/**
 * @author antsa-1 from GitHub 11 Apr 2022
 **/

public class GridGameResult extends GameResult {
	@JsonbProperty("fromX") // WinRow starts coordinates
	private int fromX;
	@JsonbProperty("fromY")
	private int fromY;
	@JsonbProperty("toX") // WinRow ends coordinates
	private int toX;
	@JsonbProperty("toY")
	private int toY;
	@JsonbProperty("token")
	private GameToken token;

	public int getFromX() {
		return fromX;
	}

	public void setFromX(int fromX) {
		this.fromX = fromX;
	}

	public int getFromY() {
		return fromY;
	}

	public void setFromY(int fromY) {
		this.fromY = fromY;
	}

	public int getToX() {
		return toX;
	}

	public void setToX(int toX) {
		this.toX = toX;
	}

	public int getToY() {
		return toY;
	}

	public void setToY(int toY) {
		this.toY = toY;
	}

	public GameToken getToken() {
		return token;
	}

	public void setToken(GameToken token) {
		this.token = token;
	}

}
