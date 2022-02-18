package com.tauhka.games.core.stats;

import java.util.logging.Logger;

import com.tauhka.games.core.User;
import com.tauhka.games.core.twodimen.GameResult;

/**
 * 
 * ELO-kindish calculator but not exactly.
 * 
 * @author antsa-1 from GitHub 22 Feb 2022
 *
 */
public class RankingCalculator {
	private static final Logger LOGGER = Logger.getLogger(RankingCalculator.class.getName());

	// ELO-based ranking sampling
	// https://medium.com/purple-theory/what-is-elo-rating-c4eb7a9061e0
	public static void updateRankings(GameResult result) {
		if (result == null) {
			throw new IllegalArgumentException("Cannot calculate ranking from nothing");
		}

		User playerA = result.getPlayerA();
		User playerB = result.getPlayerB();
		User winner = result.getPlayer();
		boolean connectFour = result.getGameMode().isConnectFour();
		Double aRanking = connectFour ? playerA.getRankingConnectFour() : playerA.getRankingTictactoe();
		Double bRanking = connectFour ? playerB.getRankingConnectFour() : playerB.getRankingTictactoe();
		Double aExpected = 1 / (1 + Math.pow(10, (bRanking - aRanking) / 400d));
		Double bExpected = 1 / (1 + Math.pow(10, (aRanking - bRanking) / 400d));
		Double newRankingA = 0d;
		Double newRankingB = 0d;
		if (playerA.equals(winner)) {
			newRankingA = aRanking + 16 * (1d - aExpected);// OK
			newRankingB = bRanking + 16 * (0d - bExpected);
		} else if (playerB.equals(winner)) {
			newRankingA = aRanking + 16 * (0d - aExpected);
			newRankingB = bRanking + 16 * (1d - bExpected);
		} else {
			// Decrease 2 points from higher ranked player and add to lower one for now..
			if (aRanking > bRanking) {
				aRanking = aRanking - 2;
				bRanking = bRanking + 2;
			} else if (aRanking < bRanking) {
				aRanking = aRanking + 2;
				bRanking = bRanking - 2;
			}
			// Equal ranking and draw -> no changes

		}
		// 100 minimum
		if (connectFour) {
			playerA.setRankingConnectFour(newRankingA < 100 ? 100d : newRankingA);
			playerB.setRankingConnectFour(newRankingB < 100 ? 100d : newRankingB);
		} else {
			playerA.setRankingTictactoe(newRankingA < 100 ? 100d : newRankingA);
			playerB.setRankingTictactoe(newRankingB < 100 ? 100d : newRankingB);
		}
		LOGGER.info("RankingCalculator:" + playerA.getName() + " from:" + aRanking + " to:" + newRankingA + " and:" + playerB.getName() + " from:" + bRanking + " to:" + newRankingB);
	}
}
