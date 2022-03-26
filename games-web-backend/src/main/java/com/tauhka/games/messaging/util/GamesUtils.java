package com.tauhka.games.messaging.util;

import java.util.ArrayList;
import java.util.List;

import com.tauhka.games.core.Game;
import com.tauhka.games.core.GameMode;

/**
 * @author antsa-1 from GitHub 25 Mar 2022
 **/

public class GamesUtils {
// Collects possible game into a list once per application restart
	public static final List<Game> games;
	static {
		games = new ArrayList<Game>();
		Game tictactoe = new Game();
		tictactoe.setGameId(GameMode.TIC_TAC_TOE);
		tictactoe.setName("TicTacToe");
		tictactoe.setGameModes(GameMode.getGamemodes(GameMode.TIC_TAC_TOE));

		Game connectFour = new Game();
		connectFour.setGameId(GameMode.CONNECT4);
		connectFour.setName("ConnectFour");
		connectFour.setGameModes(GameMode.getGamemodes(GameMode.CONNECT4));

		Game pool = new Game();
		pool.setGameId(GameMode.POOL);
		pool.setName("Pool");
		pool.setGameModes(GameMode.getGamemodes(GameMode.POOL));
		games.add(tictactoe);
		games.add(connectFour);
		games.add(pool);
	}

}
