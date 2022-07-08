package com.tauhka.games.core.timer;

/**
 * @author antsa-1 from GitHub 26 Jun 2022
 **/

//TODO from Database
public enum TimeControlIndex {
	INDEX_0(10), INDEX_1(15), INDEX_2(20), INDEX_3(30), INDEX_4(45), INDEX_5(60), INDEX_6(90), INDEX_7(240), INDEX_8(300);

	private int seconds;

	private TimeControlIndex(int seconds) {
		this.seconds = seconds;
	}

	public int getSeconds() {
		return seconds;
	}

	public boolean isQuickGame() {
		return seconds <= 20;
	}

	public static TimeControlIndex getWithIndex(int index) {
		switch (index) {
		case 0:
			return INDEX_0;
		case 1:
			return INDEX_1;
		case 2:
			return INDEX_2;
		case 3:
			return INDEX_3;
		case 4:
			return INDEX_4;
		case 5:
			return INDEX_5;
		case 6:
			return INDEX_6;
		case 7:
			return INDEX_7;
		case 8:
			return INDEX_8;
		default:
			throw new IllegalArgumentException("no such timeControlIndex:" + index);
		}
	}
}
