package com.tauhka.games.yatzy.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.tauhka.games.yatzy.Dice;
import com.tauhka.games.yatzy.Hand;
import com.tauhka.games.yatzy.HandType;
import com.tauhka.games.yatzy.ScoreCard;
import com.tauhka.games.yatzy.YatzyAI;

/**
 * @author antsa-1 from GitHub 4 Jul 2022
 **/

public class EmptyScoreCardHandSelectionTests {
	private static final UUID FIRST_ID = UUID.fromString("e05455ee-0833-445a-9ea0-5a6f116add4d");
	private static final UUID SECOND_ID = UUID.fromString("d83cc17f-2c25-4541-9ab5-99d68f7b682f");
	private static final UUID THIRD_ID = UUID.fromString("7bebe1f3-3ab3-4e6a-ad6d-6bd7911cea6e");
	private static final UUID FOURTH_ID = UUID.fromString("d93d6eb2-066b-4c83-98e4-a14a16eea268");
	private static final UUID FIFTH_ID = UUID.fromString("40dec27e-7f3a-4e60-931d-0854e3462126");

	@Test
	public void yatzyIsSelected() {
		List<Dice> dices = createDices(1, 1, 1, 1, 1);
		YatzyAI computer = new YatzyAI();
		computer.setRollsLeft(1);
		ScoreCard sc = new ScoreCard();
		Hand hand = computer.deepContemplateHands(sc, dices);
		assertEquals(HandType.YATZY, hand.getHandType());
	}

	@Test
	public void smallStraightIsSelected() {
		List<Dice> dices = createDices(5, 4, 2, 1, 3);
		YatzyAI computer = new YatzyAI();
		computer.setRollsLeft(1);
		ScoreCard sc = new ScoreCard();
		Hand hand = computer.deepContemplateHands(sc, dices);
		assertEquals(HandType.SMALL_STRAIGHT, hand.getHandType());
	}

	@Test
	public void largeStraightIsSelected() {
		List<Dice> dices = createDices(5, 4, 2, 6, 3);
		YatzyAI computer = new YatzyAI();
		computer.setRollsLeft(1);
		ScoreCard sc = new ScoreCard();
		Hand hand = computer.deepContemplateHands(sc, dices);
		assertEquals(HandType.LARGE_STRAIGHT, hand.getHandType());
	}

	@Test
	public void bigFullHouseIsSelected() {
		List<Dice> dices = createDices(4, 4, 5, 5, 5);
		YatzyAI computer = new YatzyAI();
		computer.setRollsLeft(1);
		ScoreCard sc = new ScoreCard();
		Hand hand = computer.deepContemplateHands(sc, dices);
		assertEquals(HandType.FULL_HOUSE, hand.getHandType());
	}

	@Test
	public void smallFullHouseIsNotSelectedWhenManyHandsLeft() {
		List<Dice> dices = createDices(4, 3, 4, 3, 4);
		YatzyAI computer = new YatzyAI();
		ScoreCard sc = new ScoreCard();
		computer.setRollsLeft(1);
		Hand hand = computer.deepContemplateHands(sc, dices);
		assertNull(hand.getHandType());
		assertTrue(dices.get(0).isSelected());
		assertFalse(dices.get(1).isSelected());
		assertTrue(dices.get(2).isSelected());
		assertFalse(dices.get(3).isSelected());
		assertTrue(dices.get(4).isSelected());
	}

	@Test
	public void fourOfKindIsFoundAndComputerTriesToRollForYatzy() {
		List<Dice> dices = createDices(5, 5, 5, 6, 5);
		YatzyAI computer = new YatzyAI();
		computer.setRollsLeft(1);
		ScoreCard sc = new ScoreCard();
		Hand hand = computer.deepContemplateHands(sc, dices);
		assertNull(hand.getHandType());
		assertTrue(dices.get(0).isSelected());
		assertTrue(dices.get(1).isSelected());
		assertTrue(dices.get(2).isSelected());
		assertFalse(dices.get(3).isSelected());
		assertTrue(dices.get(4).isSelected());
	}

	@Test
	public void fivesIsPrefferedOverFourOfKindInRaceCondition() {
		List<Dice> dices = createDices(5, 5, 5, 6, 5);
		YatzyAI computer = new YatzyAI();
		computer.setRollsLeft(0);
		ScoreCard sc = new ScoreCard();
		Hand hand = computer.deepContemplateHands(sc, dices);
		assertEquals(HandType.FIVES, hand.getHandType());
	}

	@Test
	public void sixesIsPrefferedOverFourOfKindInRaceCondition() {
		List<Dice> dices = createDices(6, 6, 5, 6, 6);
		YatzyAI computer = new YatzyAI();
		computer.setRollsLeft(0);
		ScoreCard sc = new ScoreCard();
		Hand hand = computer.deepContemplateHands(sc, dices);
		assertEquals(HandType.SIXES, hand.getHandType());
	}

	@Test
	public void fourOfKindIsPrefferedOverFoursInRaceCondition() {
		List<Dice> dices = createDices(4, 4, 5, 4, 4);
		YatzyAI computer = new YatzyAI();
		computer.setRollsLeft(0);
		ScoreCard sc = new ScoreCard();
		Hand hand = computer.deepContemplateHands(sc, dices);
		assertEquals(HandType.FOUR_OF_KIND, hand.getHandType());
	}

	@Test
	public void pairIsReturnedWhenNoRollsLeft() {
		List<Dice> dices = createDices(5, 4, 5, 2, 1);
		YatzyAI computer = new YatzyAI();
		computer.setRollsLeft(0);
		ScoreCard sc = new ScoreCard();
		Hand hand = computer.deepContemplateHands(sc, dices);
		assertEquals(HandType.PAIR, hand.getHandType());
	}

	@Test
	public void twoPairIsSelected() {
		List<Dice> dices = createDices(4, 6, 4, 1, 6);
		YatzyAI computer = new YatzyAI();
		computer.setRollsLeft(0);
		ScoreCard sc = new ScoreCard();
		Hand hand = computer.deepContemplateHands(sc, dices);
		assertEquals(HandType.TWO_PAIR, hand.getHandType());
	}

	private List<Dice> createDices(int first, int second, int third, int fourth, int fifth) {
		List<Dice> dices = new ArrayList<Dice>(5);
		dices.add(new Dice(FIRST_ID, first));
		dices.add(new Dice(SECOND_ID, second));
		dices.add(new Dice(THIRD_ID, third));
		dices.add(new Dice(FOURTH_ID, fourth));
		dices.add(new Dice(FIFTH_ID, fifth));
		return dices;
	}
}
