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
			List<Player> othersWithRanking = result.getOtherRankedPlayers(p.getId());
			Double newRankingA = 0d;
			for (Player other : othersWithRanking) {
				Double aRankingInitial = p.getInitialRanking();
				Double bRankingInitial = other.getInitialRanking();
				if (p.isComputerPlayer() && other.getStatus() != Status.FINISHED) {
					// No points for computer if other player timeouts
					// For10-20 seconds games , timeout is likely. User not allowed to get points by playing 1 hand, timing out and winning by score
					// If computer times out then the server has bigger problems -> no need to check computer timeout
					newRankingA = aRankingInitial;
					p.getRankingsAfter().add(newRankingA);
					continue;
				} else if (other.isComputerPlayer() && p.getStatus() != Status.FINISHED) {
					// But the player who did not finish the game gets two minus points
					newRankingA = aRankingInitial - 2;
					p.getRankingsAfter().add(newRankingA);
					continue;
				}
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
