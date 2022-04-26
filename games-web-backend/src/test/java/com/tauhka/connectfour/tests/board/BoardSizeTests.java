package com.tauhka.connectfour.tests.board;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.tauhka.games.connectfour.ConnectFourTable;
import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.User;

public class BoardSizeTests {
	@Test
	public void negativeBoardSizeThrowsException() {
		User playerA = new User("test");
		ConnectFourTable c = new ConnectFourTable(playerA, GameMode.getGameMode(21), false);
		User playerB = new User("best");
		c.setPlayerB(playerB);
		c.addGameToken(playerA, 0, 0);

		IllegalArgumentException illegalArgEx = assertThrows(IllegalArgumentException.class, () -> {
			c.addGameToken(playerB, 0, -15);
		});
		String expectedMessage = "Board length fail:-15";
		String actualMessage = illegalArgEx.getMessage();

		assertTrue(actualMessage.contains(expectedMessage));
	}

	@Test
	public void rowBeyondBoardThrowsException() {
		User playerA = new User("test");
		ConnectFourTable c = new ConnectFourTable(playerA, GameMode.getGameMode(21), false);
		User playerB = new User("best");
		c.setPlayerB(playerB);
		c.addGameToken(playerA, 0, 0);

		IllegalArgumentException illegalArgEx = assertThrows(IllegalArgumentException.class, () -> {
			c.addGameToken(playerB, 0, 115);
		});
		String expectedMessage = "Board length fail:115";
		String actualMessage = illegalArgEx.getMessage();

		assertTrue(actualMessage.contains(expectedMessage));
	}
}
