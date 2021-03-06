package com.tauhka.games.core.twodimen.util;

import com.tauhka.games.core.GameToken;
import com.tauhka.games.core.twodimen.GridGameResult;

public class WinnerChecker {

	public static GridGameResult checkWinner(GameToken[][] board, int requiredAmount) {

		GridGameResult result = checkHorizontals(board, requiredAmount);
		if (result != null) {
			return result;
		}

		result = checkVerticals(board, requiredAmount);
		if (result != null) {
			return result;
		}
		result = checkDiagonalsUpLeftToUpRight(board, requiredAmount);
		if (result != null) {
			return result;
		}
		result = checkDiagonalsUpRightToUpLeft(board, requiredAmount);
		if (result != null) {
			return result;
		}
		return null;

	}

	private static GridGameResult checkDiagonalsUpLeftToUpRight(GameToken[][] board, int requiredAmount) {
		int counter = 0;
		GameToken compToken = null;
		GridGameResult win = new GridGameResult();
		for (int x = 0; x < board.length; x++) {
			counter = 0;
			compToken = null;
			// int tempX = x;
			for (int y = 0; y < board[0].length; y++) {
				int tempX = x;
				int tempY = y;
				counter = 0;
				for (int z = 0; tempX >= 0 && tempX < board.length && tempY >= 0 && tempY < board[y].length; z++) {
					GameToken token = board[tempX][tempY];
					if (token == null) {
						// No token
						compToken = null;
						counter = 0;
					} else if (token == compToken) {
						// Token is same as previous
						compToken = token;
						counter++;
						if (counter == requiredAmount) {
							win.setToX(tempX);
							win.setToY(tempY);
							win.setToken(compToken);
							return win;
						}
					} else {
						// new token to compare
						compToken = token;
						counter = 1;
						win.setFromX(tempX);
						win.setFromY(tempY);
					}
					tempX++;
					tempY--;
				}
			}
		}
		return null;
	}

	private static GridGameResult checkDiagonalsUpRightToUpLeft(GameToken[][] board, int requiredAmount) {
		int counter = 0;
		GameToken compToken = null;
		GridGameResult win = new GridGameResult();
		for (int x = board.length - 1; x >= 0; x--) {
			counter = 0;
			compToken = null;
			// int tempX = x;
			for (int y = 0; y <= board[0].length - 1; y++) {
				int tempX = x;
				int tempY = y;
				counter = 0;
				for (int z = 0; tempX >= 0 && tempX < board.length && tempY >= 0 && tempY < board[y].length; z++) {
					GameToken token = board[tempX][tempY];
					if (token == null) {
						// No token
						compToken = null;
						counter = 0;
					} else if (token == compToken) {
						// Token is same as previous
						compToken = token;
						counter++;
						if (counter == requiredAmount) {
							win.setToX(tempX);
							win.setToY(tempY);
							win.setToken(compToken);
							return win;
						}
					} else {
						// new token to compare
						compToken = token;
						counter = 1;
						win.setFromX(tempX);
						win.setFromY(tempY);
					}
					tempX--;
					tempY--;
				}
			}
		}
		return null;
	}

	private static GridGameResult checkHorizontals(GameToken[][] board, int requiredAmount) {
		int counter = 0;
		GameToken compToken = null;
		GridGameResult win = new GridGameResult();
		for (int i = 0; i < board.length; i++) {
			counter = 0;
			compToken = null;
			for (int j = 0; j < board.length; j++) {
				GameToken token = board[i][j];

				if (token == null) {
					// No token
					compToken = null;
					counter = 0;
				} else if (token == compToken) {
					// Token is same as previous
					compToken = token;
					counter++;
					if (counter == requiredAmount) {

						win.setToX(i);
						win.setToY(j);
						win.setToken(compToken);
						return win;
					}
				} else {
					// new token to compare
					compToken = token;
					counter = 1;
					win.setFromX(i);
					win.setFromY(j);
				}
			}
		}
		return null;
	}

	private static GridGameResult checkVerticals(GameToken[][] board, int requiredAmount) {
		int counter = 0;
		GameToken compToken = null;
		GridGameResult win = new GridGameResult();
		for (int i = 0; i < board.length; i++) {
			counter = 0;
			compToken = null;
			for (int j = 0; j < board.length; j++) {
				GameToken token = board[j][i];
				if (token == null) {
					// No token
					compToken = null;
					counter = 0;
				} else if (token == compToken) {
					// Token is same as previous
					compToken = token;
					counter++;
					if (counter == requiredAmount) {
						win.setToX(j);
						win.setToY(i);
						win.setToken(compToken);
						return win;
					}
				} else {
					// new token to compare
					compToken = token;
					counter = 1;
					win.setFromX(j);
					win.setFromY(i);
				}
			}
		}
		return null;
	}
}
