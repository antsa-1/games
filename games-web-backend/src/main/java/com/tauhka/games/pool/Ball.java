package com.tauhka.games.pool;

/**
 * @author antsa-1 from GitHub 6 Mar 2022
 **/

public class Ball {
	private int number;

	public Ball(int number) {
		this.number = number;
	}

	public boolean isLower() {
		return number < 8;
	}

	public boolean isUpper() {
		return number > 8;
	}

}
