package com.tauhka.games.core.tables;

import java.time.Instant;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.GameResultType;
import com.tauhka.games.core.GameToken;
import com.tauhka.games.core.Move;
import com.tauhka.games.core.User;
import com.tauhka.games.core.twodimen.GameResult;
import com.tauhka.games.core.twodimen.GridGameResult;
import com.tauhka.games.core.twodimen.ai.TwoDimensionalBoardAdapter;
import com.tauhka.games.core.twodimen.util.WinnerChecker;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.json.bind.annotation.JsonbTypeAdapter;

/**
 * @author antsa-1 from GitHub 26 Mar 2022
 * 
 * Table for TicTacToe and basis for ConnectFour game
 **/

public class TicTacToeTable extends Table {
	private static final long serialVersionUID = 1L;
	@JsonbTypeAdapter(TwoDimensionalBoardAdapter.class)
	private GameToken board[][];
	@JsonbProperty("x")
	private int x = 0;
	@JsonbProperty("y")
	private int y = 0;
	@JsonbTransient
	private int addedTokens;

	public TicTacToeTable(User playerA, GameMode gameMode, boolean randomizeStarter, boolean registeredOnly, int timeControlIndex) {
		super(playerA, gameMode, randomizeStarter, registeredOnly, timeControlIndex, TWO_PLAYER_GAME);
		playerA.setGameToken(GameToken.X);
		super.setPlayerInTurn(playerA);
		this.startingPlayer = playerA;
		this.x = gameMode.getX();
		this.y = gameMode.getY();
		this.addedTokens = 0;
		board = new GameToken[gameMode.getX()][gameMode.getY()];
	}

	@Override
	protected Table startRematch() {
		board = new GameToken[super.getGameMode().getX()][super.getGameMode().getY()];
		super.resetRematchPlayer();
		if (startingPlayer.equals(playerA)) {
			playerA.setGameToken(GameToken.X);
			playerB.setGameToken(GameToken.O);
		} else {
			playerA.setGameToken(GameToken.O);
			playerB.setGameToken(GameToken.X);
		}
		this.addedTokens = 0;
		this.gameOver = false;
		this.startTime = Instant.now();
		super.setGameResult(null);
		return this;
	}

	public int getX() {
		return this.gameMode.getX();
	}

	public int getY() {
		return this.gameMode.getY();
	}

	@Override
	public synchronized Move playTurn(User user, Object moveInObject) {
		if (!user.equals(this.playerInTurn)) {
			throw new IllegalArgumentException("Player is not in turn in board:" + this);
			// throw new CloseWebSocketExcepetion("Player is not in turn in board:" + this);
		}
		Move moveIn = (Move) moveInObject;
		validateInputs(moveIn.getX(), moveIn.getY());
		GameToken token = this.board[moveIn.getX()][moveIn.getY()];
		if (token != null) {
			throw new IllegalArgumentException("Board already has token x:" + x + "+ y:" + y + " _" + this);
		}
		token = this.playerInTurn.getGameToken();
		this.board[moveIn.getX()][moveIn.getY()] = token;
		this.changePlayerInTurn();
		this.addedTokens++;
		Move move = new Move(moveIn.getX(), moveIn.getY());
		move.setToken(token);
		return move;
	}

	private void validateInputs(Integer x, Integer y) {
		if (x < 0 || y < 0 || x > this.board.length - 1 || y > this.board.length - 1) {
			throw new IllegalArgumentException("Board length fail, x:" + x + " y:" + y);
		}
		GameToken token = this.board[x][y];
		if (token != null) {
			throw new IllegalArgumentException("Board already has token x:" + x + "+ y:" + y + " _" + this);
		}
	}

	public GameResult checkWinAndDraw() {
		int requiredTokenConnects = this.gameMode.getRequiredConnections();
		GridGameResult result = WinnerChecker.checkWinner(this.board, requiredTokenConnects);
		if (result == null) {
			// No winner, is it a draw?
			if (isDraw()) {
				result = new GridGameResult();
				result.setResultType(GameResultType.DRAW);
				result.setStartInstant(this.startTime);
				result.setGameMode(this.gameMode);

				// this.playerInTurn = null;
				initRematchForArtificialPlayer();
				addPlayersToResult(result);
			}
			return result;
		}
		initRematchForArtificialPlayer();
		addPlayersToResult(result);
		result.setResultType(GameResultType.WIN_BY_PLAY);
		result.setGameMode(this.gameMode);
		result.setStartInstant(this.startTime);

		if (playerA.getGameToken() == result.getToken()) {
			result.setWinner(playerA);
			return result;
		}
		result.setWinner(playerB);
		return result;
	}

	private boolean isDraw() {
		return addedTokens == gameMode.getX() * gameMode.getY();
	}

	protected void addTokenCount() {
		this.addedTokens++;
	}

	@Override
	public boolean joinTableAsPlayer(User playerB) {
		this.playerB = playerB;
		this.startTime = Instant.now();
		playerB.setGameToken(GameToken.O);
		return gameShouldStartNow;
	}

	public GameToken[][] getBoard() {
		return board;
	}

	public void setBoard(GameToken[][] board) {
		this.board = board;
	}

	@Override
	public void leaveTable(User user) {
		// TODO Auto-generated method stub
		
	}
}
