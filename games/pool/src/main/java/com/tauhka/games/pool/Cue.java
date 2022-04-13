package com.tauhka.games.pool;

import javax.swing.JLabel;

import com.tauhka.games.core.Vector2d;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;

/**
 * @author antsa-1 from GitHub 27 Mar 2022
 **/

public class Cue {
	@JsonbProperty("velocity")
	private Vector2d velocity;
	@JsonbProperty("force")
	private Double force;
	@JsonbProperty("position")
	private Vector2d position;
	@JsonbProperty("angle")
	private Double angle;

	@JsonbTransient
	private JLabel imageLabel;

	public Vector2d getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector2d velocity) {
		this.velocity = velocity;
	}

	public Double getForce() {
		return force;
	}

	public void setForce(Double force) {
		this.force = force;
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

	public JLabel getImageLabel() {
		return imageLabel;
	}

	public void setImageLabel(JLabel imageLabel) {
		this.imageLabel = imageLabel;
	}

}
