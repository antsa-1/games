package com.tauhka.games.yatzy.util;

import java.util.List;

import com.tauhka.games.yatzy.Dice;
import com.tauhka.games.yatzy.Hand;

/**
 * @author antsa-1 from GitHub 2 Jul 2022
 **/

public class HandWrapper {
	private Hand mostValuable;
	private Hand secondMostValuable;
	private List<Dice> dices;

	public HandWrapper(Hand hand) {
		super();
		this.mostValuable = hand;
	}

	public Hand getMostValuable() {
		return mostValuable;
	}

	public void setMostValuable(Hand mostValuable) {
		this.mostValuable = mostValuable;
	}

	public Hand getSecondMostValuable() {
		return secondMostValuable;
	}

	public void setSecondMostValuable(Hand secondMostValuable) {
		this.secondMostValuable = secondMostValuable;
	}

	public List<Dice> getDices() {
		return dices;
	}

	public void setDices(List<Dice> dices) {
		this.dices = dices;
	}
}
