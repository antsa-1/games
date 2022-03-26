package com.tauhka.games.core.tables;

import java.util.logging.Logger;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.GameToken;
import com.tauhka.games.core.Move;
import com.tauhka.games.core.User;

public class ConnectFourTable extends TicTacToeTable {
	private static final Logger LOGGER = Logger.getLogger(ConnectFourTable.class.getName());

	public ConnectFourTable(User playerA, GameMode gameMode, boolean randomizeStarter) {
		super(playerA, gameMode, randomizeStarter);
	}

	@Override
	public synchronized Move playTurn(User user, Integer x, Integer y) {
		if (!user.equals(super.getPlayerInTurn())) {
			throw new IllegalArgumentException("Player is not in turn in board:" + this);

		}
		GameToken[][] board = super.getBoard();
		GameMode gameMode = super.getGameMode();
		int rows = gameMode.getY() - 1;
		GameToken token = null;
		while (rows >= 0) {
			token = board[rows][y];
			if (token == null) {
				board[rows][y] = user.getGameToken();

				this.changePlayerInTurn();
				super.addTokenCount();
				Move move = new Move(rows, y);
				move.setToken(user.getGameToken());
				LOGGER.fine("ConnectFour: added" + rows + " -" + y);
				return move;
			}
			rows--;
		}
		LOGGER.info("ConnectFour: Column is full, UI-should prevent this.");
		return null;
	}
}
