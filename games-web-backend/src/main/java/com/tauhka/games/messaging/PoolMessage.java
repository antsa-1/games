package com.tauhka.games.messaging;

import java.io.Serializable;

import com.tauhka.games.core.Vector2d;

import jakarta.json.bind.annotation.JsonbProperty;

/** @author antsa-1 from GitHub 26 Mar 2022 **/

public class PoolMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonbProperty("cueAngle")
	private Double cueAngle;
	@JsonbProperty("cueBallPosition")
	private Vector2d cueBallPosition;
	@JsonbProperty("canvas")
	private Vector2d canvas;

	public Vector2d getCueBallPosition() {
		return cueBallPosition;
	}

	public void setCueBallPosition(Vector2d cueBallPosition) {
		this.cueBallPosition = cueBallPosition;
	}

	public Vector2d getCanvas() {
		return canvas;
	}

	public void setCanvas(Vector2d canvas) {
		this.canvas = canvas;
	}

	public Double getCueAngle() {
		return cueAngle;
	}

	public void setCueAngle(Double cueAngle) {
		this.cueAngle = cueAngle;
	}
}
