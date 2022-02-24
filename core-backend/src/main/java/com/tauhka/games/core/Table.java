package com.tauhka.games.core;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

import com.tauhka.games.core.twodimen.ArtificialUser;
import com.tauhka.games.core.twodimen.GameResult;
import com.tauhka.games.core.twodimen.GameResultType;
import com.tauhka.games.core.twodimen.util.TwoDimensionalBoardAdapter;
import com.tauhka.games.core.twodimen.util.WinnerChecker;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.json.bind.annotation.JsonbTypeAdapter;

// Contains two players, board and spectators. Plus some logic.

public class Table {
	private static final Logger LOGGER = Logger.getLogger(Table.class.getName());

	@JsonbProperty("tableId")
	private UUID tableId;
	@JsonbProperty("playerA")
	private User playerA;
	@JsonbProperty("playerB")
	private User playerB;
	@JsonbProperty("playerInTurn")
	private User playerInTurn;
	@JsonbTransient
	private User rematchPlayer;
	@JsonbTransient()
	private User startingPlayer;
	@JsonbProperty("timer")
	private int timer = 180;
	@JsonbProperty("watchers")
	private List<User> watchers = new ArrayList<User>();
	@JsonbTypeAdapter(TwoDimensionalBoardAdapter.class)
	private GameToken board[][];
	@JsonbProperty("x")
	private int x = 0;
	@JsonbProperty("y")
	private int y = 0;
	@JsonbProperty("draws")
	private int draws = 0;
	@JsonbTransient
	private boolean randomizeStarter;
	@JsonbProperty("gameMode")
	private GameMode gameMode;
	@JsonbProperty("gameStartedInstant")
	private Instant gameStartedInstant;
	@JsonbTransient
	private int addedTokens;

	public Table(User playerA, GameMode gameMode, boolean randomizeStarter) {
		super();
		this.tableId = UUID.randomUUID();
		this.playerA = playerA;
		this.startingPlayer = playerA;

		playerA.setGameToken(GameToken.X);
		this.randomizeStarter = randomizeStarter;
		this.playerInTurn = this.playerA;
		board = new GameToken[gameMode.getX()][gameMode.getY()];
		this.gameMode = gameMode;
		this.x = this.gameMode.getX();
		this.y = this.gameMode.getY();
		this.addedTokens = 0;

	}

	@JsonbTransient
	public boolean isArtificialPlayerInTurn() {
		return this.playerInTurn != null && this.playerInTurn instanceof ArtificialUser;
	}

	private Table startRematch() {
		board = new GameToken[gameMode.getX()][gameMode.getY()];
		this.rematchPlayer = null;
		if (startingPlayer.equals(playerA)) {
			startingPlayer = playerB;
		} else {
			startingPlayer = playerA;
		}
		if (startingPlayer.equals(playerA)) {
			playerA.setGameToken(GameToken.X);
			playerB.setGameToken(GameToken.O);
		} else {
			playerA.setGameToken(GameToken.O);
			playerB.setGameToken(GameToken.X);
		}
		this.playerInTurn = startingPlayer;
		this.addedTokens = 0;
		this.gameStartedInstant = Instant.now();
		return this;
	}

	public boolean isWaitingOpponent() {
		return this.playerA != null && this.playerB == null;
	}

	public boolean isPlayerInTurn(User u) {
		if (this.playerInTurn == null) {
			return false;
		}
		return this.playerInTurn.equals(u);
	}

	public synchronized GameResult resign(User player) {
		if (this.isPlayer(player) && this.playerInTurn != null) {
			this.playerInTurn = null;
			User opponent = getOpponent(player);
			GameResult gameResult = new GameResult();
			gameResult.setStartInstant(this.gameStartedInstant);
			gameResult.setWinner(opponent);
			gameResult.setPlayerA(this.playerA);
			gameResult.setPlayerB(this.playerB);
			gameResult.setResultType(GameResultType.WIN_BY_RESIGNATION);

		}
		return null;
	}

	public int getDraws() {
		return draws;
	}

	public GameMode getGameMode() {
		return gameMode;
	}

	public void setDraws(int draws) {
		this.draws = draws;
	}

	public User getOpponent(User player) {
		if (playerA.equals(player)) {
			return playerB;
		}
		return playerA;
	}

	public boolean isPlayer(User user) {
		if (user == null) {
			return false;
		}
		return user.equals(playerA) || user.equals(playerB);
	}

	public boolean isWatcher(User user) {
		return this.watchers.contains(user);
	}

	public int getX() {
		return this.gameMode.getX();
	}

	public boolean addWatcher(User watcher) {
		if (this.playerA == null || this.playerB == null) {
			return false;
		}
		if (!this.watchers.contains(watcher)) {
			this.watchers.add(watcher);

			return true;
		}
		return false;
	}

	public int getY() {
		return this.gameMode.getY();
	}

	// Tuplaklikit jne. -> synchronized plus

	public synchronized Move addGameToken(User user, Integer x, Integer y) {
		if (!user.equals(this.playerInTurn)) {
			throw new IllegalArgumentException("Player is not in turn in board:" + this);
			// throw new CloseWebSocketExcepetion("Player is not in turn in board:" + this);
		}
		GameToken token = this.board[x][y];
		if (token != null) {
			throw new IllegalArgumentException("Board already has token x:" + x + "+ y:" + y + " _" + this);
		}
		token = this.playerInTurn.getGameToken();
		this.board[x][y] = token;
		this.changePlayerInTurn();
		this.addedTokens++;
		Move move = new Move(x, y);
		move.setToken(token);
		return move;
	}

	public GameResult checkWinAndDraw() {
		int requiredTokenConnects = this.gameMode.getRequiredConnections();
		GameResult result = WinnerChecker.checkWinner(this.board, requiredTokenConnects);
		if (result == null) {
			// No winner, is it a draw?
			if (isDraw()) {
				result = new GameResult();
				result.setResultType(GameResultType.DRAW);
				result.setStartInstant(this.gameStartedInstant);
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
		result.setStartInstant(this.gameStartedInstant);

		if (playerA.getGameToken() == result.getToken()) {
			result.setWinner(playerA);
			return result;
		}
		result.setWinner(playerB);
		return result;
	}

	private void addPlayersToResult(GameResult result) {
		// Order does not actually matter
		result.setPlayerA(this.playerA);
		result.setPlayerB(this.playerB);
	}

	private boolean isDraw() {
		return addedTokens == gameMode.getX() * gameMode.getY();
	}

	public void initRematchForArtificialPlayer() {

		// PlayerA can't be Artificial player since it does not create tables
		if (this.playerB != null && this.playerB instanceof ArtificialUser) {
			this.suggestRematch(this.playerB);
		}
	}

	public int getTimer() {
		return timer;
	}

	public void setTimer(int timer) {
		this.timer = timer;
	}

	protected void addTokenCount() {
		this.addedTokens++;
	}

	@JsonbTransient
	public List<User> getUsers() {
		List<User> users = new ArrayList<User>();
		if (playerA != null) {
			users.add(playerA);
		}
		if (playerB != null) {
			users.add(playerB);
		}

		users.addAll(watchers);
		return users;
	}

	public boolean removePlayerIfExist(User user) {
		if (playerA != null && playerA.equals(user)) {
			playerA = null;
			this.playerInTurn = null;
			return true;
		} else if (playerB != null && playerB.equals(user)) {
			playerB = null;
			this.playerInTurn = null;
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(tableId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Table other = (Table) obj;
		return Objects.equals(tableId, other.tableId);
	}

	public boolean removeWatcherIfExist(User user) {
		return watchers.remove(user);
	}

	public User getPlayerA() {
		return playerA;
	}

	public UUID getTableId() {
		return tableId;
	}

	public User getPlayerB() {
		return playerB;
	}

	public boolean isRandomizeStarter() {
		return randomizeStarter;
	}

	public void setRandomizeStarter(boolean randomizeStarter) {
		this.randomizeStarter = randomizeStarter;
	}

	public User getPlayerInTurn() {
		return playerInTurn;
	}

	public void changePlayerInTurn() {
		if (this.playerInTurn.equals(playerA)) {
			this.playerInTurn = playerB;
			return;
		}
		this.playerInTurn = playerA;
	}

	public void setPlayerB(User playerB) {
		this.playerB = playerB;
		playerB.setGameToken(GameToken.O);

		this.gameStartedInstant = Instant.now();
	}

	public List<User> getWatchers() {
		return watchers;
	}

	@Override
	public String toString() {
		return "TicTacToeTable [id=" + tableId + ", playerA=" + playerA + ", playerB=" + playerB + "]";
	}

	public GameToken[][] getBoard() {
		return board;
	}

	public void setBoard(GameToken[][] board) {
		this.board = board;
	}

	public synchronized boolean suggestRematch(User user) {
		if (this.rematchPlayer == null) {
			this.rematchPlayer = user;
			return false;
		}
		if (this.rematchPlayer.equals(user)) {
			return false;
		}
		this.startRematch();
		return true;
	}

	public Instant getGameStartedInstant() {
		return gameStartedInstant;
	}

}
