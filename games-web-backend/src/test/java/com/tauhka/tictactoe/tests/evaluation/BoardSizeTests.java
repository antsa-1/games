package com.tauhka.tictactoe.tests.evaluation;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.Table;
import com.tauhka.games.core.User;

public class BoardSizeTests {
	@Test
	public void negativeBoardSizeThrowsException() {
		User playerA = new User("test");
		Table c = new Table(playerA, GameMode.getGameMode(1), false);
		User playerB = new User("best");
		c.setPlayerB(playerB);
		c.addGameToken(playerA, 0, 0);

		IllegalArgumentException illegalArgEx = assertThrows(IllegalArgumentException.class, () -> {
			c.addGameToken(playerB, 0, -15);
		});
		String expectedMessage = "Board length fail, x0 y:-15";
		String actualMessage = illegalArgEx.getMessage();

		assertTrue(actualMessage.contains(expectedMessage));
	}

	@Test
	public void tooBigRowThrowExpection() {
		User playerA = new User("test");
		Table c = new Table(playerA, GameMode.getGameMode(1), false);
		User playerB = new User("best");
		c.setPlayerB(playerB);
		c.addGameToken(playerA, 0, 0);

		IllegalArgumentException illegalArgEx = assertThrows(IllegalArgumentException.class, () -> {
			c.addGameToken(playerB, 0, 115);
		});
		String expectedMessage = "Board length fail, x0 y:115";
		String actualMessage = illegalArgEx.getMessage();

		assertTrue(actualMessage.contains(expectedMessage));
	}
}
