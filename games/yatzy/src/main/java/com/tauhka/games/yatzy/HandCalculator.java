package com.tauhka.games.yatzy;

import java.util.Arrays;
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
	private static final int YATZY = 50;

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
			return findNumberCount(6, hand) * 6;
		case PAIR:
			Set<Integer> pair = searchAllPairsFromBiggestToSmallest(hand);
			if (pair.size() == 0)
				return NO_POINTS;
			return pair.iterator().next() * 2;
		case TWO_PAIR:
			Set<Integer> pairs = searchAllPairsFromBiggestToSmallest(hand);
			if (pairs.size() != 2)
				return NO_POINTS;
			Iterator<Integer> iterator = pairs.iterator();
			while (iterator.hasNext()) {
				total += iterator.next() * 2;
			}
			return total;
		case TRIPS:
			int trips = getNumberWhichExistAtLeastTimes(3, hand);
			return trips > 0 ? trips * 3 : NO_POINTS;
		case QUADS:
			int quads = getNumberWhichExistAtLeastTimes(4, hand);
			return quads > 0 ? quads * 4 : NO_POINTS;
		case YATZY:
			int yatzy = getNumberWhichExistAtLeastTimes(5, hand);
			return yatzy > 0 ? YATZY : NO_POINTS;
		case SMALL_STRAIGHT:
			boolean smallStraight = isStraight(SMALL_STRAIGHT, hand);
			return smallStraight ? 15 : NO_POINTS;
		case LARGE_STRAIGHT:
			boolean large = isStraight(LARGE_STRAIGHT, hand);
			return large ? 20 : NO_POINTS;
		case FULL_HOUSE:
			return findFullHouse(hand);
		case CHANCE:
			return addAllNumbers(hand);
		default:
			throw new IllegalArgumentException("No such hand in calculations:" + hand.getHandType());
		}
	}

	private static int addAllNumbers(Hand hand) {
		return hand.getDices().stream().map(Dice::getNumber).collect(Collectors.summingInt(Integer::intValue));
	}

	private static int findFullHouse(Hand hand) {
		List<Integer> numbers = hand.getDices().stream().map(Dice::getNumber).collect(Collectors.toList());
		Set<Integer> ints = new HashSet<Integer>(numbers);
		if (ints.size() != 2) {
			return NO_POINTS;
		}
		Iterator<Integer> iterator = ints.iterator();
		int firstNumber = iterator.next();
		int secondNumber = iterator.next();
		int freqFirst = Collections.frequency(numbers, firstNumber);
		int freqSecond = Collections.frequency(numbers, secondNumber);
		if (freqFirst == 2 && freqSecond == 3) {
			return firstNumber * freqFirst + secondNumber * freqSecond;
		}
		if (freqFirst == 3 && freqSecond == 2) {
			return firstNumber * freqFirst + secondNumber * freqSecond;
		}

		return NO_POINTS;
	}

	private static final List<Integer> SMALL_STRAIGHT = Arrays.asList(1, 2, 3, 4, 5);
	private static final List<Integer> LARGE_STRAIGHT = Arrays.asList(6, 2, 3, 4, 5);

	private static boolean isStraight(List<Integer> straight, Hand hand) {
		List<Integer> numbers = hand.getDices().stream().map(Dice::getNumber).collect(Collectors.toList());
		return numbers.containsAll(straight);
	}

	// Usage -> callees have minimumTimes of 3 -> using 5 dices, only one number can exist which has >= frequency
	private static Integer getNumberWhichExistAtLeastTimes(int minimumTimes, Hand hand) {
		List<Integer> numbers = hand.getDices().stream().map(Dice::getNumber).collect(Collectors.toList());
		Set<Integer> ints = new HashSet<Integer>(numbers);
		for (Integer i : ints) {
			int freq = Collections.frequency(numbers, i);
			if (freq >= minimumTimes) {
				return i;
			}
		}
		return -1;
	}

	private static Set<Integer> searchAllPairsFromBiggestToSmallest(Hand hand) {
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
