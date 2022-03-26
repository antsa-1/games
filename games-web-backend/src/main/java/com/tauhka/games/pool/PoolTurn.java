package com.tauhka.games.pool;

import com.tauhka.games.core.Vector2d;

import jakarta.json.bind.annotation.JsonbProperty;

/**
 * @author antsa-1 from GitHub 25 Mar 2022
 **/

public class PoolTurn {
	@JsonbProperty("cueBall")
	private Vector2d velocity;
	private Vector2d position;
	private Double angle;

	public Vector2d getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector2d velocity) {
		this.velocity = velocity;
	}

	public Vector2d getPosition() {
		return position;
	}

	public void setPosition(Vector2d position) {
		this.position = position;
	}

	public Double getAngle() {
		return angle;
	}

	public void setAngle(Double angle) {
		this.angle = angle;
	}

}
