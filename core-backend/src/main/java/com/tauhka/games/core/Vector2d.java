package com.tauhka.games.core;

import java.util.Objects;

import jakarta.json.bind.annotation.JsonbProperty;

/** @author antsa-1 from GitHub 25 Mar 2022 **/

public class Vector2d {
	@JsonbProperty("x")
	private Double x;
	@JsonbProperty("y")
	private Double y;

	public Vector2d() {
		// For messagedecoding empty required
	}

	public Vector2d(Double x, Double y) {
		this.x = x;
		this.y = y;
	}

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

	public void add(Vector2d vec) {
		this.x += vec.getX().doubleValue();
		this.y += vec.getY().doubleValue();
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vector2d other = (Vector2d) obj;
		return Objects.equals(x, other.x) && Objects.equals(y, other.y);
	}

	@Override
	public String toString() {
		return "Vector2d [x=" + x + ", y=" + y + "]";
	}

}
