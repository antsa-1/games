package com.tauhka.games.pool;

import com.tauhka.games.core.Vector2d;

import jakarta.json.bind.annotation.JsonbProperty;

/**
 * @author antsa-1 from GitHub 6 Mar 2022
 **/

public class CueBall extends Ball {

	@JsonbProperty("angle")
	private Double angle;
	@JsonbProperty("force")
	private Double force;

	public CueBall() {
		// For deserialization
	}

	public CueBall(int number, Color color, Vector2d position, Double diameter) {
		super(number, color, position, diameter);
	}

	public CueBall(int number, Color color, Vector2d position, Double diameter, String imageName) {
		this(number, color, position, diameter);
	}

	public Double getAngle() {
		return angle;
	}

	public void setAngle(Double angle) {
		this.angle = angle;
	}

	public Double getForce() {
		return force;
	}

	public void setForce(Double force) {
		this.force = force;
	}

}
