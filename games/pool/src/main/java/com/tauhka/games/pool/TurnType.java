package com.tauhka.games.pool;

/**
 * @author antsa-1 from GitHub 12 Apr 2022
 **/

public enum TurnType {
	PLAY("PLAY"), POCKET_SELECTION("POCKET_SELECTION"), HANDBALL("HANDBALL");

	private String text;

	private TurnType(String text) {
		this.text = text;
	}

	public String getAsText() {
		return this.text;
	}

}
