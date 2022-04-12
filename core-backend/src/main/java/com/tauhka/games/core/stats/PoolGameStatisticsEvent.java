package com.tauhka.games.core.stats;

import com.tauhka.games.core.twodimen.PoolTurnStats;

/**
 * @author antsa-1 from GitHub 11 Apr 2022
 **/

public class PoolGameStatisticsEvent {
	private PoolTurnStats gameResult;

	public PoolTurnStats getGameResult() {
		return gameResult;
	}

	public void setGameResult(PoolTurnStats gameResult) {
		this.gameResult = gameResult;
	}

	@Override
	public String toString() {
		return "PoolGameStatisticsEvent [gameResult=" + gameResult + "]";
	}
}
