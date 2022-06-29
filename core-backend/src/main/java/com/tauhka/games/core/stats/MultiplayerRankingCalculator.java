package com.tauhka.games.core.stats;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author antsa-1 from GitHub 26 Jun 2022
 **/

public class MultiplayerRankingCalculator {

	public static void calculateNewRankings(Result result) {
		if (result == null) {
			throw new IllegalArgumentException("Cannot calculate ranking from nothing");
		}
		for (Player p : result.getPlayers()) {
			List<Player> others = getOtherPlayers(result, p);
			Double newRankingA = 0d;
//			Double newRankingB = 0d;
			for (Player other : others) {
				Double aRankingInitial = p.getInitialRanking();
				Double bRankingInitial = other.getInitialRanking();
				Double aExpectedPercentage = 1 / (1 + Math.pow(10, (bRankingInitial - aRankingInitial) / 400d));
//				Double bExpectedPercentage = 1 / (1 + Math.pow(10, (aRankingInitial - bRankingInitial) / 400d));

				if (p.getFinishPosition() < other.getFinishPosition()) {
					newRankingA = aRankingInitial + 16 * (1d - aExpectedPercentage);// OK
					p.getRankingsAfter().add(newRankingA);
//					newRankingB = bRankingInitial + 16 * (0d - bExpectedPercentage);
				} else if (p.getFinishPosition() > other.getFinishPosition()) {
					newRankingA = aRankingInitial + 16 * (0d - aExpectedPercentage);
					p.getRankingsAfter().add(newRankingA);
//					newRankingB = bRankingInitial + 16 * (1d - bExpectedPercentage);
				} else {
					p.getRankingsAfter().add(aRankingInitial);
				}
				p.getRankingsAfter().add(newRankingA);
			}
			Double sum = p.getRankingsAfter().stream().mapToDouble(Double::doubleValue).sum();
			p.setFinalRanking(sum / result.getPlayers().size());
		}

	}

	private static List<Player> getOtherPlayers(Result r, Player p) {
		return r.getPlayers().stream().filter(player -> !player.getId().equals(p.getId())).collect(Collectors.toList());
	}
}
