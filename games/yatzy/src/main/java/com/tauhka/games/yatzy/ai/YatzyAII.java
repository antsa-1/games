package com.tauhka.games.yatzy.ai;

import java.util.List;

import com.tauhka.games.yatzy.Dice;

/**
 * @author antsa-1 from GitHub 4 Jul 2022
 **/

public interface YatzyAII {

	public default void selectAllDices(List<Dice> dices) {
		dices.forEach(dice -> dice.select());
	}

	public default void selectNumbers(List<Dice> dices, int numberToSelect) {
		for (Dice d : dices) {
			if (d.getNumber() == numberToSelect) {
				d.select();
			}
		}
	}

	public default void selectFirstNumber(List<Dice> dices, int numberToSelect) {
		for (Dice d : dices) {
			if (d.getNumber() == numberToSelect) {
				d.select();
				return;
			}
		}
	}
}
