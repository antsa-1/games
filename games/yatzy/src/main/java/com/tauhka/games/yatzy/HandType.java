package com.tauhka.games.yatzy;

/**
 * @author antsa-1 from GitHub 12 May 2022
 **/

public enum HandType {
	PAIR, TWO_PAIR, TRIPS, QUADS, YATZY, SMALL_STRAIGHT, LARGE_STRAIGHT, ONES, TWOS, THREES, FOURS, FIVES, SIXES, FULL_HOUSE, CHANCE;

	public static boolean isSubTotalType(HandType type) {
		return type == ONES || type == TWOS || type == THREES || type == FOURS || type == FIVES || type == SIXES;
	}
}
