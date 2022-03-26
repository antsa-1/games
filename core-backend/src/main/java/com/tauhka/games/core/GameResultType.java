package com.tauhka.games.core;

public enum GameResultType {

	WIN_BY_PLAY(1), WIN_BY_TIME(2), WIN_BY_RESIGNATION(3), WIN_BY_DISCONNECT(4), DRAW(5);

	private int asInt = 0;

	public int getAsInt() {
		return asInt;
	}

	private GameResultType(int type) {
		this.asInt = type;
	}

	public static boolean isWin(GameResultType type) {
		if (type == null) {
			throw new IllegalArgumentException("Should not be null");
		}
		if (type == WIN_BY_PLAY) {
			return true;
		} else if (type == WIN_BY_TIME) {
			return true;
		} else if (type == WIN_BY_RESIGNATION) {
			return true;
		} else if (type == WIN_BY_DISCONNECT) {
			return true;
		}
		return false;
	}
}
