package com.tauhka.games.pool;

import com.tauhka.games.core.Vector2d;

import jakarta.json.bind.annotation.JsonbProperty;

/**
 * @author antsa-1 from GitHub 27 Mar 2022
 **/

public class Canvas {
	@JsonbProperty("size")
	private Vector2d size;

	public Canvas(Vector2d size) {
		this.size = size;
	}

	public Vector2d getSize() {
		return size;
	}

	public void setSize(Vector2d size) {
		this.size = size;
	}

}
