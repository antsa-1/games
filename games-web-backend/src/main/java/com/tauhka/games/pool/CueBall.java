package com.tauhka.games.pool;

import com.tauhka.games.core.Vector2d;

import jakarta.json.bind.annotation.JsonbProperty;

/**
 * @author antsa-1 from GitHub 6 Mar 2022
 **/

public class CueBall {
	@JsonbProperty("velocity")
	private Vector2d velocity;
	@JsonbProperty("position")
	private Vector2d position;
	@JsonbProperty("angle")
	private Double angle;
	@JsonbProperty("force")
	private Double force;

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

	public Vector2d getPosition() {
		return position;
	}

	public void setPosition(Vector2d position) {
		this.position = position;
	}

	public Double getForce() {
		return force;
	}

	public void setForce(Double force) {
		this.force = force;
	}

}
