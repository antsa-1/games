package com.tauhka.games.core.twodimen.ai;

import java.util.List;

import com.tauhka.games.core.GameToken;
import com.tauhka.games.core.Move;
import com.tauhka.games.core.tables.TicTacToeTable;
import com.tauhka.games.core.twodimen.GameResult;
import com.tauhka.games.core.twodimen.util.MoveHelper;
import com.tauhka.games.core.twodimen.util.WinnerChecker;

public interface CommonAI {

	public default Move calculateBestMove(TicTacToeTable table) {
		Move move = null;
		List<Move> availableMoves = MoveHelper.getAvailableMoves(table);
		move = checkOwnWinningCondition(table, availableMoves);
		if (move != null) {
			return move;
		}
		move = checkOpponentWinningCondition(table, availableMoves);
		if (move != null) {
			return move;
		}

		return MoveEvaluator.calculateMove(table, availableMoves);
	}

	private Move checkOpponentWinningCondition(TicTacToeTable table, List<Move> availableMoves) {
		GameToken tokenToBePlayed = table.getPlayerInTurn().getGameToken();
		// Check opponent token, since opponent is not in turn
		if (tokenToBePlayed == GameToken.O) {
			tokenToBePlayed = GameToken.X;
		} else {
			tokenToBePlayed = GameToken.O;
		}
		for (Move move : availableMoves) {
			GameToken[][] deepCloned = deepCopy(table.getBoard());
			deepCloned[move.getX()][move.getY()] = tokenToBePlayed;
			GameResult result = WinnerChecker.checkWinner(deepCloned, table.getGameMode().getRequiredConnections());
			if (result != null) {
				return move;
			}
		}
		return null;
	}

	private Move checkOwnWinningCondition(TicTacToeTable table, List<Move> availableMoves) {
		GameToken tokenToBePlayed = table.getPlayerInTurn().getGameToken();
		for (Move move : availableMoves) {
			GameToken[][] deepCloned = deepCopy(table.getBoard());
			deepCloned[move.getX()][move.getY()] = tokenToBePlayed;
			GameResult result = WinnerChecker.checkWinner(deepCloned, table.getGameMode().getRequiredConnections());
			if (result != null) {
				return move;
			}
		}
		return null;
	}

	private <T> GameToken[][] deepCopy(GameToken[][] matrix) {
		return java.util.Arrays.stream(matrix).map(el -> el.clone()).toArray(c -> matrix.clone());
	}
}
