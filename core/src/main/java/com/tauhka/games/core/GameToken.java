package com.tauhka.games.core;

public enum GameToken {
	X("X"), O("O"); // just a token, X can be used in ConnectFour also, UI-representation varies

	private String token;

	private GameToken(String text) {
		this.token = text;
	}

	public String getAsText() {
		return this.token;
	}

}
