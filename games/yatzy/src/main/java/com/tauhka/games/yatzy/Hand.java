package com.tauhka.games.yatzy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author antsa-1 from GitHub 12 May 2022
 **/

public class Hand {

	private List<Dice> dices;
	private HandType handType;

	public Hand() {
		dices = new ArrayList<Dice>(5);
	}

	public boolean isLocked() {
		Optional<Dice> locked = dices.stream().filter(dice -> dice == null).findFirst();
		return locked.isEmpty();
	}

}
