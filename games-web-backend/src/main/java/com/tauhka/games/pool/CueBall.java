package com.tauhka.games.pool;

import com.tauhka.games.core.Vector2d;

import jakarta.json.bind.annotation.JsonbProperty;

/**
 * @author antsa-1 from GitHub 6 Mar 2022
 **/

public class CueBall {
	@JsonbProperty("velocity")
	private Vector2d velocity;
	@JsonbProperty("startPosition")
	private Vector2d startPosition;
	@JsonbProperty("endPosition")
	private Vector2d endPosition;
	@JsonbProperty("angle")
	private Double angle;

	public Vector2d getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector2d velocity) {
		this.velocity = velocity;
	}

	public Double getAngle() {
		return angle;
	}

	public void setAngle(Double angle) {
		this.angle = angle;
	}

	public Vector2d getStartPosition() {
		return startPosition;
	}

	public void setStartPosition(Vector2d startPosition) {
		this.startPosition = startPosition;
	}

	public Vector2d getEndPosition() {
		return endPosition;
	}

	public void setEndPosition(Vector2d endPosition) {
		this.endPosition = endPosition;
	}

}
