package com.tauhka.games.yatzy.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.tauhka.games.yatzy.Dice;
import com.tauhka.games.yatzy.Hand;
import com.tauhka.games.yatzy.HandType;
import com.tauhka.games.yatzy.util.HandCalculator;

/**
 * @author antsa-1 from GitHub 16 Jun 2022
 **/

public class HandCalculatorTests {

	@Test
	public void onesAreFound() {
		Hand h = createDices(HandType.ONES, 5, 4, 2, 1, 1);
		assertEquals(2, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void onesAreNotFound() {
		Hand h = createDices(HandType.ONES, 5, 4, 2, 6, 4);
		assertEquals(0, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void twossAreFound() {
		Hand h = createDices(HandType.TWOS, 2, 4, 2, 2, 2);
		assertEquals(8, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void twossAreNotFound() {
		Hand h = createDices(HandType.TWOS, 5, 4, 6, 6, 4);
		assertEquals(0, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void threesAreFound() {
		Hand h = createDices(HandType.THREES, 3, 3, 3, 3, 3);
		assertEquals(15, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void threesAreNotFound() {
		Hand h = createDices(HandType.THREES, 5, 4, 6, 6, 4);
		assertEquals(0, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void foursAreFound() {
		Hand h = createDices(HandType.FOURS, 5, 3, 3, 3, 4);
		assertEquals(4, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void foursAreNotFound() {
		Hand h = createDices(HandType.FOURS, 5, 2, 6, 6, 1);
		assertEquals(0, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void fivesAreFound() {
		Hand h = createDices(HandType.FIVES, 5, 3, 3, 5, 4);
		assertEquals(10, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void fivesAreNotFound() {
		Hand h = createDices(HandType.FIVES, 1, 2, 6, 6, 1);
		assertEquals(0, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void sixesAreFound() {
		Hand h = createDices(HandType.SIXES, 6, 3, 6, 5, 6);
		assertEquals(18, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void sixesAreNotFound() {
		Hand h = createDices(HandType.SIXES, 1, 2, 3, 3, 1);
		assertEquals(0, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void noPairIsFound() {
		Hand h = createDices(HandType.PAIR, 1, 2, 3, 4, 5);
		assertEquals(0, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void pairIsFoundInTheBeginningOfList() {
		Hand h = createDices(HandType.PAIR, 1, 1, 3, 4, 5);
		assertEquals(2, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void pairIsFoundInTheMiddleOfList() {
		Hand h = createDices(HandType.PAIR, 2, 3, 3, 4, 5);
		assertEquals(6, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void pairIsFoundInTheEndOfList() {
		Hand h = createDices(HandType.PAIR, 2, 1, 3, 6, 6);
		assertEquals(12, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void pairIsFoundWhenAllNumbersAreSame() {
		Hand h = createDices(HandType.PAIR, 4, 4, 4, 4, 4);
		assertEquals(8, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void highestPairIsFound() {
		Hand h = createDices(HandType.PAIR, 2, 2, 3, 5, 5);
		assertEquals(10, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void highestPairIsFound2() {
		Hand h = createDices(HandType.PAIR, 2, 4, 6, 4, 6);
		assertEquals(12, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void twoPairIsFound() {
		Hand h = createDices(HandType.TWO_PAIR, 2, 2, 3, 5, 5);
		assertEquals(14, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void twoPairIsFoundFromFullHouse() {
		Hand h = createDices(HandType.TWO_PAIR, 4, 5, 4, 5, 5);
		assertEquals(18, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void noTwoPairIsFound() {
		Hand h = createDices(HandType.TWO_PAIR, 4, 5, 4, 4, 4);
		assertEquals(0, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void noTwoPairIsFound2() {
		Hand h = createDices(HandType.TWO_PAIR, 1, 2, 3, 4, 5);
		assertEquals(0, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void tripsIsFoundFromBeginning() {
		Hand h = createDices(HandType.TRIPS, 1, 1, 1, 4, 5);
		assertEquals(3, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void tripsFromMiddlePositions() {
		Hand h = createDices(HandType.TRIPS, 1, 2, 2, 2, 4);
		assertEquals(6, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void tripsFromFiveOfAKindFound() {
		Hand h = createDices(HandType.TRIPS, 5, 5, 5, 5, 5);
		assertEquals(15, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void tripsFromFourOfAKindFound() {
		Hand h = createDices(HandType.TRIPS, 2, 4, 2, 5, 2);
		assertEquals(6, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void tripsFromMiddlePositionFound() {
		Hand h = createDices(HandType.TRIPS, 2, 3, 3, 3, 2);
		assertEquals(9, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void tripsNotFound() {
		Hand h = createDices(HandType.TRIPS, 6, 5, 4, 3, 2);
		assertEquals(0, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void quadsFromEndPositionFound() {
		Hand h = createDices(HandType.QUADS, 2, 1, 1, 1, 1);
		assertEquals(4, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void quadsNotFound() {
		Hand h = createDices(HandType.QUADS, 5, 5, 5, 6, 6);
		assertEquals(0, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void yatzyNotFound() {
		Hand h = createDices(HandType.YATZY, 5, 5, 5, 5, 6);
		assertEquals(0, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void yatzyFound() {
		Hand h = createDices(HandType.YATZY, 2, 2, 2, 2, 2);
		assertEquals(50, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void smallStraightFound() {
		Hand h = createDices(HandType.SMALL_STRAIGHT, 5, 4, 1, 3, 2);
		assertEquals(15, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void smallStraightNotFound() {
		Hand h = createDices(HandType.SMALL_STRAIGHT, 6, 5, 4, 3, 2);
		assertEquals(0, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void smallStraightNotFound2() {
		Hand h = createDices(HandType.SMALL_STRAIGHT, 1, 1, 2, 3, 4);
		assertEquals(0, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void largeStraightFound() {
		Hand h = createDices(HandType.LARGE_STRAIGHT, 4, 2, 3, 6, 5);
		assertEquals(20, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void largeStraightNotFound() {
		Hand h = createDices(HandType.LARGE_STRAIGHT, 1, 2, 3, 4, 5);
		assertEquals(0, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void fullHouseFound() {
		Hand h = createDices(HandType.FULL_HOUSE, 6, 5, 5, 6, 5);
		assertEquals(27, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void fullHouseFound2() {
		Hand h = createDices(HandType.FULL_HOUSE, 2, 2, 2, 3, 3);
		assertEquals(12, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void fullHouseNotFound() {
		Hand h = createDices(HandType.FULL_HOUSE, 2, 2, 2, 2, 3);
		assertEquals(0, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void fullHouseNotFound2() {
		Hand h = createDices(HandType.FULL_HOUSE, 6, 1, 5, 5, 5);
		assertEquals(0, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void chanceCalculation() {
		Hand h = createDices(HandType.CHANCE, 6, 1, 5, 5, 5);
		assertEquals(22, HandCalculator.calculateHandValue(h));
	}

	@Test
	public void chanceCalculation2() {
		Hand h = createDices(HandType.CHANCE, 1, 1, 2, 2, 2);
		assertEquals(8, HandCalculator.calculateHandValue(h));
	}

	private Hand createDices(HandType type, int first, int second, int third, int fourth, int fifth) {
		List<Dice> dices = new ArrayList<Dice>(5);
		dices.add(new Dice(first));
		dices.add(new Dice(second));
		dices.add(new Dice(third));
		dices.add(new Dice(fourth));
		dices.add(new Dice(fifth));
		return new Hand(type, dices);
	}
}
