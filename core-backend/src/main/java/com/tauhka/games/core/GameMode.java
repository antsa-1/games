package com.tauhka.games.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.json.bind.annotation.JsonbTransient;

public final class GameMode {
	private final static List<GameMode> GAMEMODES;
	private final int id;
	private final String name;
	private final int y, x;
	private final int requiredConnections;
	private final int gameNumber;
	public static final Integer TIC_TAC_TOE = 1;
	public static final Integer CONNECT4 = 2;
	public static final Integer POOL = 3;
	public static final Integer YATZY = 4;
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
		GameMode gm21 = new GameMode(CONNECT4, 21, 10, 10, 4);// ConnectFour 10x10 board
		GameMode gm22 = new GameMode(POOL, 30, "8-ball");// 8-ball
		GameMode gm40 = new GameMode(YATZY, 40, "Yatzy");// Yatzy

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
		GAMEMODES.add(gm22);
		GAMEMODES.add(gm40);
	}

	public GameMode(int gameNumber, int gameMode, String name) {
		super();
		this.id = gameMode;
		this.y = -1;
		this.x = -1;
		this.name = name;
		this.gameNumber = gameNumber;
		requiredConnections = -1;
	}

	public GameMode(int gameNumber, int gameMode, int y, int x, int requiredConnectionsToWin) {
		super();
		this.id = gameMode;
		this.y = y;
		this.x = x;
		this.requiredConnections = requiredConnectionsToWin;
		this.gameNumber = gameNumber;
		String s = "";
		if (requiredConnectionsToWin > 0) {
			s = " connect " + requiredConnectionsToWin;
		}
		this.name = Integer.toString(x) + "x" + Integer.toString(y) + s;

	}

	@JsonbTransient
	public boolean isConnectFour() {
		return this.gameNumber == 2;
	}

	@JsonbTransient
	public boolean isTicTacToe() {
		return this.gameNumber == 1;
	}

	@JsonbTransient
	public boolean isEightBall() {
		return this.gameNumber == 3;
	}

	@JsonbTransient
	public boolean isYatzy() {
		return this.gameNumber == 4;
	}

	public static String getBoardDescription(int gameMode) {

		GameMode mode = GameMode.getGameMode(gameMode);

		return mode.getX() + "x" + mode.getY() + " /" + mode.getRequiredConnections();
	}

	public static GameMode getGameMode(int id) {
		if (id < 1 || id > 100) {
			throw new IllegalArgumentException("Wrong gameMode:" + id);
		}
		Optional<GameMode> gameMode = GAMEMODES.stream().filter(mode -> mode.id == id).findFirst();
		if (gameMode.isEmpty()) {
			throw new IllegalArgumentException("No such gameMode:" + id);
		}
		return gameMode.get();
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

	public int getGameNumber() {
		return gameNumber;
	}

	public int getX() {
		return x;
	}

	public static List<GameMode> getGamemodes(int gameNumber) {
		return GAMEMODES.stream().filter(mode -> mode.gameNumber == gameNumber).collect(Collectors.toList());
	}

	public static List<GameMode> getGameModes() {
		return GameMode.GAMEMODES;
	}

	@Override
	public String toString() {
		return "GameMode [id=" + id + ", name=" + name + ", gameNumber=" + gameNumber + "]";
	}
}
