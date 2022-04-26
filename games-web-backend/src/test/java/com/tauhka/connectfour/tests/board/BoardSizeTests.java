package com.tauhka.connectfour.tests.board;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.Move;
import com.tauhka.games.core.User;
import com.tauhka.games.core.tables.ConnectFourTable;
//TODO next time fix
public class BoardSizeTests {
	@Test
	public void negativeBoardSizeThrowsException() {
		User playerA = new User("test");
		ConnectFourTable c = new ConnectFourTable(playerA, GameMode.getGameMode(21), false, false, 0);
		User playerB = new User("best");
		c.joinTableAsPlayer(playerB);
		// c.addGameToken(playerA, 0, 0);

		IllegalArgumentException illegalArgEx = assertThrows(IllegalArgumentException.class, () -> {
			Move move = new Move(0, -15);
			c.playTurn(playerB, move);
		});
		String expectedMessage = "Board length fail:-15";
		String actualMessage = illegalArgEx.getMessage();

		assertTrue(actualMessage.contains(expectedMessage));
	}

	@Test
	public void rowBeyondBoardThrowsException() {
		User playerA = new User("test");
		ConnectFourTable c = new ConnectFourTable(playerA, GameMode.getGameMode(21), false, false, 0);
		User playerB = new User("best");
		c.joinTableAsPlayer(playerB);
		Move move = new Move(0, 0);
		c.playTurn(playerA, move);
		IllegalArgumentException illegalArgEx = assertThrows(IllegalArgumentException.class, () -> {
			Move move2 = new Move(0, 115);
			c.playTurn(playerB, move2);

		});
		String expectedMessage = "Board length fail:115";
		String actualMessage = illegalArgEx.getMessage();

		assertTrue(actualMessage.contains(expectedMessage));
	}
}
