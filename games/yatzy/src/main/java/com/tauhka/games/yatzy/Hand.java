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
	private Integer points;

	public Hand() {
		dices = new ArrayList<Dice>(5);
	}

	private boolean includeInSubtotal() {
		return HandType.isSubTotalType(handType);
	}

	public boolean isLocked() {
		Optional<Dice> locked = dices.stream().filter(dice -> dice == null).findFirst();
		return locked.isEmpty();
	}

	public int calculateSubTotalPoints() {
		if (includeInSubtotal()) {
			return calculatePoints();
		}
		return 0;
	}

	public int calculatePoints() {
		if (points != null) {
			return points;
		}
		// TODO handType based calculation
		return 1;
	}

	public HandType getHandType() {
		return handType;
	}

}
