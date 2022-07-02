package com.tauhka.games.yatzy.util;

import java.util.List;

import com.tauhka.games.yatzy.Dice;
import com.tauhka.games.yatzy.Hand;

/**
 * @author antsa-1 from GitHub 2 Jul 2022
 **/

public class HandWrapper {
	private double probability;
	private Hand hand;
	private List<Dice> dices;

	public HandWrapper(Hand hand) {
		super();
		this.hand = hand;
	}

	public Hand getHand() {
		return hand;
	}

	public List<Dice> getDices() {
		return dices;
	}

	public void setDices(List<Dice> dices) {
		this.dices = dices;
	}

	public void setHand(Hand hand) {
		this.hand = hand;
	}

	public double getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability = probability;
	}

}
