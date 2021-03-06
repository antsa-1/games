package com.tauhka.games.pool;

/**
 * @author antsa-1 from GitHub 5 Apr 2022
 **/

public enum TurnResult {
	HANDBALL, HANDBALL_FAIL, EIGHT_BALL_IN_POCKET_OK, EIGHT_BALL_IN_POCKET_FAIL, CONTINUE_TURN, CHANGE_TURN, SELECT_POCKET, ASK_POCKET_SELECTION;

	public static boolean isDecisive(TurnResult tr) {
		return tr == EIGHT_BALL_IN_POCKET_FAIL || tr == EIGHT_BALL_IN_POCKET_OK;
	}

	public static boolean isDecisive(String tr) {
		return tr.equals(EIGHT_BALL_IN_POCKET_OK.toString()) || tr.equals(EIGHT_BALL_IN_POCKET_FAIL.toString());
	}
}
