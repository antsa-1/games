package com.tauhka.games.core;

import java.util.Objects;

import jakarta.json.bind.annotation.JsonbProperty;

/** @author antsa-1 from GitHub 25 Mar 2022 **/

public class Vector2d {
	@JsonbProperty("x")
	public double x;
	@JsonbProperty("y")
	public double y;

	public Vector2d() {
		// For messagedecoding empty required
	}

	public Vector2d(Double x, Double y) {
		this.x = x;
		this.y = y;
	}

	/* public Double getX() { return x; }
	 * 
	 * public void setX(Double x) { this.x = x; }
	 * 
	 * public Double getY() { return y; }
	 * 
	 * public void setY(Double y) { this.y = y; }
	 * 
	 * public void add(Vector2d vec) { this.x += vec.getX().doubleValue(); this.y += vec.getY().doubleValue(); }
	 * 
	 * @Override public int hashCode() { return Objects.hash(x, y); } */


	public boolean isZero() {
		boolean retVal = this.x == 0.0d && this.y == 0.0d;
	//	System.out.println("isZero:x" + " y:" + this.y + " =" + retVal);
		return retVal;
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
		return Double.doubleToLongBits(x) == Double.doubleToLongBits(other.x) && Double.doubleToLongBits(y) == Double.doubleToLongBits(other.y);
	}

	@Override
	public String toString() {
		return "[x=" + x + ", y=" + y + "]";
	}

}
