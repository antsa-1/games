package com.tauhka.games.pool;

/**
 * @author antsa-1 from GitHub 5 Apr 2022
 **/

public enum TurnResult {
	HANDBALL, EIGHT_BALL_IN_POCKET_OK, EIGHT_BALL_IN_POCKET_FAIL, CONTINUE_TURN, CHANGE_TURN, SELECT_POCKET;

	public static boolean isDecisive(TurnResult tr) {
		return tr == EIGHT_BALL_IN_POCKET_FAIL || tr == EIGHT_BALL_IN_POCKET_OK;
	}
}
