package com.tauhka.games.core;

import java.util.ArrayList;
import java.util.List;

public final class GameMode {
	private final static List<GameMode> GAMEMODES;
	private final int id;
	private final String name;
	private final int y, x;
	private final int requiredConnections;
	private final int gameId;
	private static final Integer TIC_TAC_TOE = 1;
	private static final Integer CONNECT4 = 2;
	static {
		// Constructor tells number meanings
		GAMEMODES = new ArrayList<GameMode>();
		GameMode gm = new GameMode(TIC_TAC_TOE, 1, 3, 3, 3);// TicTacToes
		GameMode gm2 = new GameMode(TIC_TAC_TOE, 2, 5, 5, 4);// TicTacToes
		GameMode gm3 = new GameMode(TIC_TAC_TOE, 3, 7, 7, 4);// TicTacToes
		GameMode gm4 = new GameMode(TIC_TAC_TOE, 4, 10, 10, 5);// TicTacToes
		GameMode gm5 = new GameMode(TIC_TAC_TOE, 5, 20, 20, 5);// TicTacToes
		GameMode gm6 = new GameMode(TIC_TAC_TOE, 6, 25, 25, 5);// TicTacToes
		GameMode gm7 = new GameMode(TIC_TAC_TOE, 7, 30, 30, 5);// TicTacToes
		GameMode gm8 = new GameMode(TIC_TAC_TOE, 8, 40, 40, 5);// TicTacToes
		GameMode gm20 = new GameMode(CONNECT4, 20, 7, 7, 4);// ConnectFour 7x7 board
		GameMode gm21 = new GameMode(CONNECT4, 20, 10, 10, 4);// ConnectFour 10x10 board

		GAMEMODES.add(gm);
		GAMEMODES.add(gm2);
		GAMEMODES.add(gm3);
		GAMEMODES.add(gm4);
		GAMEMODES.add(gm5);
		GAMEMODES.add(gm6);
		GAMEMODES.add(gm7);
		GAMEMODES.add(gm8);
		GAMEMODES.add(gm20);
		GAMEMODES.add(gm21);
	}

	public GameMode(int gameId, int gameModeId, int y, int x, int requiredConnectionsToWin) {
		super();
		this.id = gameModeId;
		this.y = y;
		this.x = x;
		this.requiredConnections = requiredConnectionsToWin;
		this.gameId = gameId;
		this.name = Integer.toString(x) + "x" + Integer.toString(y);
	}

	public static GameMode getGameMode(int id) {
		if (id < 1 || id > GAMEMODES.size()) {
			throw new IllegalArgumentException("Wrong gameMode:" + id);
		}
		return GAMEMODES.get(id - 1);
	}

	public int getId() {
		return id;
	}

	public int getRequiredConnections() {
		return requiredConnections;
	}

	public String getName() {
		return name;
	}

	public int getY() {
		return y;
	}

	public int getGameId() {
		return gameId;
	}

	public int getX() {
		return x;
	}

	public static List<GameMode> getGameModes() {
		return GameMode.GAMEMODES;
	}
}
