package com.tauhka.games.core.stats;

import java.util.List;

/**
 * @author antsa-1 from GitHub 26 Jun 2022
 **/

public class MultiplayerRankingCalculator {

	public static void calculateNewRankings(Result result) {
		if (result == null) {
			throw new IllegalArgumentException("Cannot calculate ranking from nothing");
		}
		for (Player p : result.getRankingPlayers()) {
//			if (p.getId() == null) {
//				continue;
//			}
			List<Player> othersWithRanking = result.getOtherRankedPlayers(p.getId());
			Double newRankingA = 0d;
			for (Player other : othersWithRanking) {
				Double aRankingInitial = p.getInitialRanking();
				Double bRankingInitial = other.getInitialRanking();
				Double aExpectedPercentage = 1 / (1 + Math.pow(10, (bRankingInitial - aRankingInitial) / 400d));
				if (p.getFinishPosition() < other.getFinishPosition()) {
					newRankingA = aRankingInitial + 16 * (1d - aExpectedPercentage);// OK
				} else if (p.getFinishPosition() > other.getFinishPosition()) {
					newRankingA = aRankingInitial + 16 * (0d - aExpectedPercentage);
				} else {
					// Equal scores
					if (aRankingInitial > bRankingInitial) {
						newRankingA = aRankingInitial - 2;
					} else if (aRankingInitial < bRankingInitial) {
						newRankingA = aRankingInitial + 2;
					} else {
						newRankingA = aRankingInitial;
					}
				}
				p.getRankingsAfter().add(newRankingA);
			}
			if (p.getRankingsAfter().size() > 0) {
				Double sum = p.getRankingsAfter().stream().mapToDouble(Double::doubleValue).sum();
				p.setFinalRanking(sum / p.getRankingsAfter().size());
			}
		}
	}

}
