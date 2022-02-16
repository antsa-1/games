package com.tauhka.games.core.twodimen;

public enum GameResultType {

	WIN_BY_PLAY, WIN_BY_TIME, WIN_BY_RESIGNATION, DRAW, WIN_BY_DISCONNECT;

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
