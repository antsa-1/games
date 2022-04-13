package com.tauhka.games.pool;

import java.util.List;

import com.tauhka.games.core.User;

/**
 * @author antsa-1 from GitHub 1 Apr 2022
 **/

public class PoolPlayer extends User {

	private List<Ball> pocketedBalls;

	public List<Ball> getPocketedBalls() {
		return pocketedBalls;
	}

	public void setPocketedBalls(List<Ball> pocketedBalls) {
		this.pocketedBalls = pocketedBalls;
	}

}
