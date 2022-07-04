package com.tauhka.games.yatzy.ai;

import java.util.List;

import com.tauhka.games.yatzy.Dice;
import com.tauhka.games.yatzy.Hand;

/**
 * @author antsa-1 from GitHub 4 Jul 2022
 **/

public class ContemplationResult {
	public List<Dice> copies;
	private Hand hand;

	public List<Dice> getCopies() {
		return copies;
	}

	public void setCopies(List<Dice> copies) {
		this.copies = copies;
	}

	public Hand getHand() {
		return hand;
	}

	public void setHand(Hand hand) {
		this.hand = hand;
	}

}
