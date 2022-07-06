package com.tauhka.games.core.timer;

/**
 * @author antsa-1 from GitHub 26 Jun 2022
 **/

//TODO from Database
public enum TimeControlIndex {
	INDEX_0(10), INDEX_1(20), INDEX_2(30), INDEX_3(45), INDEX_4(60), INDEX_5(90), INDEX_6(240), INDEX_7(300);

	private int seconds;

	private TimeControlIndex(int seconds) {
		this.seconds = seconds;
	}

	public int getSeconds() {
		return seconds;
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
		default:
			throw new IllegalArgumentException("no such timeControlIndex:" + index);
		}
	}
}
