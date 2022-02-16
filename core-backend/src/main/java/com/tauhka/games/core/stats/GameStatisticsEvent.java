package com.tauhka.games.core.stats;

import com.tauhka.games.core.twodimen.GameResult;

/**
 * 
 * Statistics payload about played games for database
 *
 */
@IGameStatistics
public class GameStatisticsEvent {

	private GameResult gameResult;

	public GameResult getGameResult() {
		return gameResult;
	}

	public void setGameResult(GameResult gameResult) {
		this.gameResult = gameResult;
	}

	@Override
	public String toString() {
		return "GameStatisticsEvent [gameResult=" + gameResult + "]";
	}

}
