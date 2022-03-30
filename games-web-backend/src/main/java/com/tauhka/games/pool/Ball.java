package com.tauhka.games.pool;

import com.tauhka.games.core.Vector2d;

/**
 * @author antsa-1 from GitHub 27 Mar 2022
 **/

public class Ball implements PoolComponent {

	enum Color {
		YELLOW, RED, BLACK, WHITE
	}

	private int number;
	private Color color;
	private Vector2d position;
	private Double diameter;
	private Vector2d velocity;

	public Ball() {
		// For deserialization
	}

	public Ball(int number, Color color, Vector2d position, Double diameter) {
		this.number = number;
		this.color = color;
		this.position = position;
		this.diameter = diameter;
	}

	public boolean isMoving() {
		return !this.velocity.isZero();
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Vector2d getPosition() {
		return position;
	}

	public void setPosition(Vector2d position) {
		this.position = position;
	}

	public Double getDiameter() {
		return diameter;
	}

	public void setDiameter(Double diameter) {
		this.diameter = diameter;
	}

	public Vector2d getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector2d velocity) {
		this.velocity = velocity;
	}

	@Override
	public String toString() {
		return "Ball [number=" + number + ", position=" + position + ", velocity=" + velocity + "]";
	}

}
