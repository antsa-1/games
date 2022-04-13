package com.tauhka.games.pool;

import com.tauhka.games.core.Vector2d;

/**
 * @author antsa-1 from GitHub 31 Mar 2022
 **/

public final class PathWay {
	private final Vector2d top;
	private final Vector2d bottom;

	public PathWay(Vector2d top, Vector2d bottom) {
		this.top = top;
		this.bottom = bottom;
	}

	public Vector2d getTop() {
		return top;
	}

	public Vector2d getBottom() {
		return bottom;
	}

}
