package com.tauhka.tictactoe.tests.evaluation;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.Move;
import com.tauhka.games.core.User;
import com.tauhka.games.core.tables.Table;
import com.tauhka.games.core.tables.TicTacToeTable;

public class BoardSizeTestsTicTacToe {
	@Test
	public void negativeBoardSizeThrowsException() {
		User playerA = new User("test");
		Table c = new TicTacToeTable(playerA, GameMode.getGameMode(1), false, false, 0);
		User playerB = new User("best");
		c.joinTableAsPlayer(playerB);
		// c.addGameToken(playerA, 0, 0);

		IllegalArgumentException illegalArgEx = assertThrows(IllegalArgumentException.class, () -> {
			Move move = new Move(0, -15);
			c.playTurn(playerA, move);
		});
		String expectedMessage = "Board length fail, x:0 y:-15";
		String actualMessage = illegalArgEx.getMessage();

		assertTrue(actualMessage.contains(expectedMessage));
	}

	@Test
	public void tooBigRowThrowExpection() {
		User playerA = new User("test");
		Table c = new TicTacToeTable(playerA, GameMode.getGameMode(1), false, false, 0);
		User playerB = new User("best");
		c.joinTableAsPlayer(playerB);
		// c.addGameToken(playerA, 0, 0);

		IllegalArgumentException illegalArgEx = assertThrows(IllegalArgumentException.class, () -> {
			Move move = new Move(0, 1155);
			c.playTurn(playerA, move);
		});
		String expectedMessage = "Board length fail, x:0 y:115";
		String actualMessage = illegalArgEx.getMessage();

		assertTrue(actualMessage.contains(expectedMessage));
	}
}
