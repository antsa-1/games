package com.tauhka.games.core.stats;

import java.util.logging.Logger;

import com.tauhka.games.core.User;
import com.tauhka.games.core.twodimen.ArtificialUser;
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
		User winner = result.getWinner();
		boolean connectFour = result.getGameMode().isConnectFour();
		Double aRankingInitial = connectFour ? playerA.getRankingConnectFour() : playerA.getRankingTictactoe();
		Double bRankingInitial = connectFour ? playerB.getRankingConnectFour() : playerB.getRankingTictactoe();
		playerA.setInitialCalculationsRank(aRankingInitial);
		playerB.setInitialCalculationsRank(bRankingInitial);
		Double aExpectedPercentage = 1 / (1 + Math.pow(10, (bRankingInitial - aRankingInitial) / 400d));
		Double bExpectedPercentage = 1 / (1 + Math.pow(10, (aRankingInitial - bRankingInitial) / 400d));
		Double newRankingA = 0d;
		Double newRankingB = 0d;
		if (playerA.equals(winner)) {
			newRankingA = aRankingInitial + 16 * (1d - aExpectedPercentage);// OK
			newRankingB = bRankingInitial + 16 * (0d - bExpectedPercentage);
		} else if (playerB.equals(winner)) {
			newRankingA = aRankingInitial + 16 * (0d - aExpectedPercentage);
			newRankingB = bRankingInitial + 16 * (1d - bExpectedPercentage);
		} else {
			// Decrease 2 points from higher ranked player and add to lower one for now..
			if (aRankingInitial > bRankingInitial) {
				newRankingA = aRankingInitial - 2;
				newRankingB = bRankingInitial + 2;
			} else if (aRankingInitial < bRankingInitial) {
				newRankingA = aRankingInitial + 2;
				newRankingB = bRankingInitial - 2;
			} else {
				// Equal ranking and draw -> no changes
				newRankingA = aRankingInitial;
				newRankingB = bRankingInitial;
			}

		}
		// 100 minimum
		if (connectFour) {
			playerA.setRankingConnectFour(newRankingA < 100 ? 100d : newRankingA);
			playerB.setRankingConnectFour(newRankingB < 100 ? 100d : newRankingB);
		} else {
			playerA.setRankingTictactoe(newRankingA < 100 ? 100d : newRankingA);
			playerB.setRankingTictactoe(newRankingB < 100 ? 100d : newRankingB);
		}
		LOGGER.info("RankingCalculator:" + playerA.getName() + " from:" + aRankingInitial + " to:" + newRankingA + " and:" + playerB.getName() + " from:" + bRankingInitial + " to:" + newRankingB);
	}
}
