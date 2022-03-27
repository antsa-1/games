package com.tauhka.games.core;

import jakarta.json.bind.annotation.JsonbProperty;

/** @author antsa-1 from GitHub 25 Mar 2022 **/

public class Vector2d {
	@JsonbProperty("x")
	private Double x;
	@JsonbProperty("y")
	private Double y;

	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Double getY() {
		return y;
	}

	public void setY(Double y) {
		this.y = y;
	}

}
