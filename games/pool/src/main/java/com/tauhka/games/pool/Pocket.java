package com.tauhka.games.pool;

import com.tauhka.games.core.Vector2d;

/**
 * @author antsa-1 from GitHub 31 Mar 2022
 **/

public final class Pocket {

	private final Vector2d center;
	private final double radius;
	private final PathWay pathWayRight;
	private final PathWay pathwayLeft;
	private boolean containsEightBall;

	public Pocket(Vector2d center, double radius, PathWay pathWayRight, PathWay pathwayLeft) {
		super();
		this.center = center;
		this.radius = radius;
		this.pathWayRight = pathWayRight;
		this.pathwayLeft = pathwayLeft;
		this.containsEightBall = false;
	}

	public Vector2d getCenter() {
		return center;
	}

	public double getRadius() {
		return radius;
	}

	public PathWay getPathWayRight() {
		return pathWayRight;
	}

	public PathWay getPathwayLeft() {
		return pathwayLeft;
	}

	public boolean hasEightBall() {
		return containsEightBall;
	}

	public void setContainsEightBall(boolean containsEightBall) {
		this.containsEightBall = containsEightBall;
	}

	@Override
	public String toString() {
		return "Pocket [center=" + center + ", radius=" + radius + ", pathWayRight=" + pathWayRight + ", pathwayLeft=" + pathwayLeft + ", containsEightBall=" + containsEightBall + "]";
	}

}
