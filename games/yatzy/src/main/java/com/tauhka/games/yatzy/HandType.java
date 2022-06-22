package com.tauhka.games.yatzy;

/**
 * @author antsa-1 from GitHub 12 May 2022
 **/

public enum HandType {
	ONES(1), TWOS(2), THREES(3), FOURS(4), FIVES(5), SIXES(6), PAIR(7), TWO_PAIR(8), TRIPS(9), FULL_HOUSE(10), SMALL_STRAIGHT(11), LARGE_STRAIGHT(12), QUADS(13), CHANCE(14), YATZY(15);

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
		if (type.equals(TWO_PAIR.getAsInt()))
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
		if (type.equals(SIXES.getAsInt()))
			return SIXES;
		if (type.equals(FULL_HOUSE.getAsInt()))
			return FULL_HOUSE;
		if (type.equals(CHANCE.getAsInt()))
			return CHANCE;
		throw new IllegalArgumentException("No such handtype:" + type);
	}

	public static boolean isSubTotalType(HandType type) {
		return type == ONES || type == TWOS || type == THREES || type == FOURS || type == FIVES || type == SIXES;
	}
}
