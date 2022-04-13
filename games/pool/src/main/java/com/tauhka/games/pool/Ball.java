package com.tauhka.games.pool;

import com.tauhka.games.core.Vector2d;

/**
 * @author antsa-1 from GitHub 27 Mar 2022
 **/

public class Ball implements PoolComponent {


	private int number;
	private Color color;
	protected Vector2d position;
	private Vector2d relativePosition;
	private double diameter;
	private double radius;
	private Vector2d velocity;
	private boolean inPocket;

	public Ball() {
		// For deserialization
	}

	public Ball(int number, Color color, Vector2d position, Double diameter) {
		this.number = number;
		this.color = color;
		this.position = position;
		this.diameter = diameter;
		this.radius = diameter / 2;
	}

	public boolean isMoving() {
		return !this.velocity.isZero();
	}

	public int getNumber() {
		return number;
	}

	public Vector2d getRelativePosition() {
		return relativePosition;
	}

	public void setRelativePosition(Vector2d relativePosition) {
		this.relativePosition = relativePosition;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public boolean isLower() {
		return this.number < 8;
	}

	public Color getColor() {
		return color;
	}

	public boolean isEightBall() {
		return number == 8;
	}

	public boolean isSimilar(Ball ball) {
		if (ball == null) {
			return false;
		}
		return ball.getColor() == this.getColor();
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

	public boolean isInPocket() {
		return inPocket;
	}

	public void setInPocket(boolean inPocket) {
		this.inPocket = inPocket;
	}

	public void setVelocity(Vector2d velocity) {
		this.velocity = velocity;
	}

	public Double getRadius() {
		return radius;
	}

	public void setRadius(Double radius) {
		this.radius = radius;
	}

	@Override
	public String toString() {
		return "Ball [" + number + ", position=" + position + ", velocity=" + velocity + "]";
	}

}
