package com.tauhka.games.yatzy;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author antsa-1 from GitHub 16 Jun 2022
 **/

public class HandCalculator {
	private static final int NO_POINTS = 0;

	public static int calculateHandValue(Hand hand) {
		int total = 0;
		switch (hand.getHandType()) {
		case ONES:
			return findNumberCount(1, hand);
		case TWOS:
			return findNumberCount(2, hand) * 2;
		case THREES:
			return findNumberCount(3, hand) * 3;
		case FOURS:
			return findNumberCount(4, hand) * 4;
		case FIVES:
			return findNumberCount(5, hand) * 5;
		case SIXES:
			return findNumberCount(6, hand) * 5;
		case PAIR:
			Set<Integer> pair = searchAllPairs(hand);
			if (pair.size() == 0)
				return NO_POINTS;
			return pair.iterator().next() * 2;
		case TWO_PAIR:
			Set<Integer> pairs = searchAllPairs(hand);
			if (pairs.size() != 2)
				return NO_POINTS;
			Iterator<Integer> iterator = pairs.iterator();
			while (iterator.hasNext()) {
				total += iterator.next() * 2;
			}
			return total;
		case TRIPS:
			Set<Integer> trips = searchTrips(hand);
			if (trips.size() != 1) {
				return NO_POINTS;
			}
			total = trips.iterator().next() * 3;
			return total;
		default:
			throw new IllegalArgumentException("No such in calculations:" + hand.getHandType());
		}
	}

	private static Set<Integer> searchTrips(Hand hand) {
		return null;
	}

	private static Set<Integer> searchAllPairs(Hand hand) {
		List<Integer> numbers = hand.getDices().stream().map(Dice::getNumber).collect(Collectors.toList());
		Set<Integer> pairs = new HashSet<Integer>();
		SortedSet<Integer> sortedSet = new TreeSet<Integer>(Collections.reverseOrder());
		sortedSet.addAll(numbers.stream().sorted().filter(number -> !pairs.add(number)).collect(Collectors.toSet()));
		return sortedSet;
	}

	private static int findNumberCount(int number, Hand hand) {
		Stream<Dice> stream = hand.getDices().stream();
		return (int) stream.filter(dice -> dice.getNumber() == number).count();
	}
}
