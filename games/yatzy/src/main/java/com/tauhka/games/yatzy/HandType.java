package com.tauhka.games.yatzy;

/**
 * @author antsa-1 from GitHub 12 May 2022
 **/

public enum HandType {
	PAIR(1), TWO_PAIR(2), TRIPS(3), QUADS(4), YATZY(5), SMALL_STRAIGHT(6), LARGE_STRAIGHT(7), ONES(8), TWOS(9), THREES(10), FOURS(11), FIVES(12), SIXES(13), FULL_HOUSE(14), CHANCE(15);

	private final int asInt;

	private HandType(int type) {
		this.asInt = type;
	}

	public int getAsInt() {
		return asInt;
	}

	public static HandType getHandType(Integer type) {
		if (type.equals(PAIR.getAsInt()))
			return PAIR;
		if (type.equals(PAIR.getAsInt()))
			return TWO_PAIR;
		if (type.equals(TRIPS.getAsInt()))
			return TRIPS;
		if (type.equals(QUADS.getAsInt()))
			return QUADS;
		if (type.equals(YATZY.getAsInt()))
			return YATZY;
		if (type.equals(SMALL_STRAIGHT.getAsInt()))
			return SMALL_STRAIGHT;
		if (type.equals(LARGE_STRAIGHT.getAsInt()))
			return LARGE_STRAIGHT;
		if (type.equals(ONES.getAsInt()))
			return ONES;
		if (type.equals(TWOS.getAsInt()))
			return TWOS;
		if (type.equals(THREES.getAsInt()))
			return THREES;
		if (type.equals(FOURS.getAsInt()))
			return FOURS;
		if (type.equals(FIVES.getAsInt()))
			return FIVES;
		if (type.equals(FULL_HOUSE.getAsInt()))
			return FULL_HOUSE;
		throw new IllegalArgumentException("No such handtype:" + type);
	}

	public static boolean isSubTotalType(HandType type) {
		return type == ONES || type == TWOS || type == THREES || type == FOURS || type == FIVES || type == SIXES;
	}
}
