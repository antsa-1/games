package com.tauhka.games.yatzy.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.tauhka.games.yatzy.Dice;
import com.tauhka.games.yatzy.Hand;
import com.tauhka.games.yatzy.HandCalculator;
import com.tauhka.games.yatzy.HandType;

/**
 * @author antsa-1 from GitHub 16 Jun 2022
 **/

public class HandCalculatorTests {

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
