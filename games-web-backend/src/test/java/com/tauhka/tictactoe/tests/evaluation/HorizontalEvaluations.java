package com.tauhka.tictactoe.tests.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.GameToken;
import com.tauhka.games.core.Move;
import com.tauhka.games.core.ai.ArtificialUser;
import com.tauhka.games.core.tables.TicTacToeTable;
import com.tauhka.games.core.twodimen.ai.MoveEvaluator;
import com.tauhka.games.core.twodimen.util.MoveHelper;

//--add-exports org.junit.platform.commons/org.junit.platform.commons.util=ALL-UNNAMED --add-exports org.junit.platform.commons/org.junit.platform.commons.logging=ALL-UNNAMED
public class HorizontalEvaluations {
	@Test
	public void firstRow() {
		ArtificialUser a = new ArtificialUser();
		a.setGameToken(GameToken.O);
		ArtificialUser b = new ArtificialUser();
		a.setGameToken(GameToken.X);
		GameMode mode = GameMode.getGameMode(4);
		TicTacToeTable table = new TicTacToeTable(a, GameMode.getGameMode(4), false);
		table.joinTableAsPlayer(b);
		GameToken[][] board = new GameToken[mode.getX()][mode.getY()];
		board[4][0] = GameToken.X;
		board[1][1] = GameToken.O;
		board[2][0] = GameToken.X;
		board[3][1] = GameToken.O;
		table.setBoard(board);
		List<Move> availableMoves = MoveHelper.getAvailableMoves(table);
		// printBoard(table.getBoard());
		Move expectedMove = new Move(5, 5);

		assertEquals(expectedMove, MoveEvaluator.calculateMove(table, availableMoves));
	}

	@Test
	public void firstVertical() {
		ArtificialUser a = new ArtificialUser();
		a.setGameToken(GameToken.O);
		ArtificialUser b = new ArtificialUser();
		a.setGameToken(GameToken.X);
		GameMode mode = GameMode.getGameMode(4);
		TicTacToeTable table = new TicTacToeTable(a, GameMode.getGameMode(4), false);
		table.joinTableAsPlayer(b);
		GameToken[][] board = new GameToken[mode.getX()][mode.getY()];
		board[4][4] = GameToken.X;
		board[1][1] = GameToken.O;
		board[4][5] = GameToken.X;
		board[3][1] = GameToken.O;
		table.setBoard(board);
		List<Move> availableMoves = MoveHelper.getAvailableMoves(table);
		// printBoard(table.getBoard());
		Move expectedMove = new Move(4, 3);
		assertEquals(expectedMove, MoveEvaluator.calculateMove(table, availableMoves));
	}

	public void printBoard(GameToken board[][]) {
		for (int x = 0; x < board.length; x++) {
			for (int y = 0; y < board.length; y++) {
				if (board[y][x] != null)
					System.out.print(board[y][x] + " ");
				else {
					System.out.print(" _");
				}
				if (y == board.length - 1) {
				}
			}
		}
	}
}
