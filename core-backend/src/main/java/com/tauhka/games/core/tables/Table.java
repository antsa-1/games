package com.tauhka.games.core.tables;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.GameResultType;
import com.tauhka.games.core.User;
import com.tauhka.games.core.ai.AI;
import com.tauhka.games.core.twodimen.GameResult;
import com.tauhka.games.core.util.Constants;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;

// Base class for extending tables

public abstract class Table implements Serializable {
	private static final long serialVersionUID = 2422118498940197289L;

	private static final Logger LOGGER = Logger.getLogger(Table.class.getName());

	@JsonbProperty("tableId")
	private UUID tableId;
	@JsonbProperty("playerA")
	protected User playerA;
	@JsonbProperty("playerB")
	protected User playerB;
	@JsonbProperty("playerInTurn")
	protected User playerInTurn;
	@JsonbTransient
	protected User rematchPlayer;
	@JsonbTransient
	protected User startingPlayer;
	@JsonbProperty("timer")
	protected int timer = 180;
	@JsonbProperty("watchers")
	protected List<User> watchers = new ArrayList<User>();

	@JsonbProperty("draws")
	protected int draws = 0;
	@JsonbProperty("randomizeStarter")
	protected boolean randomizeStarter;
	@JsonbProperty("timeControlIndex")
	protected int timeControlIndex;

	@JsonbProperty("registeredOnly")
	protected boolean registeredOnly;

	@JsonbProperty("gameMode")
	protected GameMode gameMode;
	@JsonbProperty("gameStartedInstant")
	protected Instant gameStartedInstant;
	@JsonbTransient
	protected boolean gameOver;

	public Table(User playerA, GameMode gameMode, boolean randomizeStarter, boolean registeredOnly, int timeControlIndex) {
		this.tableId = UUID.randomUUID();
		this.playerA = playerA;
		this.gameMode = gameMode;
		this.randomizeStarter = randomizeStarter;
		if (!randomizeStarter) {
			this.playerInTurn = playerA;
		}
		this.registeredOnly = registeredOnly;
		this.timeControlIndex = timeControlIndex;
	}

	public abstract GameResult checkWinAndDraw();

	public abstract Object playTurn(User user, Object o);

	protected abstract Table startRematch();

	@JsonbTransient
	public boolean isArtificialPlayerInTurn() {
		return this.playerInTurn != null && this.playerInTurn instanceof AI;
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
			this.gameOver = true;
			User opponent = getOpponent(player);
			GameResult gameResult = new GameResult();
			gameResult.setStartInstant(this.gameStartedInstant);
			gameResult.setWinner(opponent);
			gameResult.setPlayerA(this.playerA);
			gameResult.setPlayerB(this.playerB);
			gameResult.setResigner(player);
			gameResult.setResultType(GameResultType.WIN_BY_RESIGNATION);
			return gameResult;
		}
		throw new IllegalArgumentException("Resign not possible" + player);
	}

	public int getDraws() {
		return draws;
	}

	public boolean isGameOver() {
		return gameOver;
	}

	public void setGameOver(boolean gameOver) {
		this.gameOver = gameOver;
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

	public int getTimeControlIndex() {
		return timeControlIndex;
	}

	public void setTimeControlIndex(int timeControlIndex) {
		this.timeControlIndex = timeControlIndex;
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

	protected void addPlayersToResult(GameResult result) {
		// Order does not actually matter
		result.setPlayerA(this.playerA);
		result.setPlayerB(this.playerB);
	}

	public void initRematchForArtificialPlayer() {

		// PlayerA can't be Artificial player since it does not create tables
		if (this.playerB != null && this.playerB instanceof AI) {
			this.suggestRematch(this.playerB);
		}
	}

	public int getTimer() {
		return timer;
	}

	public void setTimer(int timer) {
		this.timer = timer;
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

	public void setPlayerInTurn(User player) {
		playerInTurn = player;
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

	public synchronized void joinTableAsPlayer(User playerB) {
		if (this.registeredOnly && playerB.getName().startsWith(Constants.GUEST_LOGIN_NAME)) {
			// Also guest players can use this option atm..
			throw new IllegalArgumentException("Only registered players allowed to join to play." + playerB.getName() + " table:" + this);
		}
		this.playerB = playerB;
		this.gameStartedInstant = Instant.now();
		if (this.randomizeStarter) {
			int i = ThreadLocalRandom.current().nextInt(1, 1001);
			if (i > 500) {
				this.startingPlayer = playerA;
				playerInTurn = playerA;
			} else {
				this.startingPlayer = playerB;
				playerInTurn = playerB;
			}
		} else {
			this.startingPlayer = playerA;
			playerInTurn = playerA;
		}
		System.out.println("JoinedTableAsPlayer:" + this);
	}

	public List<User> getWatchers() {
		return watchers;
	}

	@Override
	public String toString() {
		return "Table [tableId=" + tableId + ", playerA=" + playerA + ", playerB=" + playerB + ", playerInTurn=" + playerInTurn + ", rematchPlayer=" + rematchPlayer + ", startingPlayer=" + startingPlayer + ", randomizeStarter="
				+ randomizeStarter + ", gameMode=" + gameMode + ", gameOver=" + gameOver + "]";
	}

	protected void resetRematchPlayer() {
		rematchPlayer = null;
	}

	public boolean isRegisteredOnly() {
		return registeredOnly;
	}

	public synchronized boolean suggestRematch(User user) {
		if (!isPlayer(user)) {
			throw new IllegalArgumentException("Rematchplayer is not a player in the table:" + user);
		}
		if (this.rematchPlayer == null) {
			this.rematchPlayer = user;
			return false;
		}
		if (this.rematchPlayer.equals(user)) {
			return false;
		}
		if (this.startingPlayer.equals(playerA)) {
			this.startingPlayer = playerB;
			this.playerInTurn = playerB;
		} else {
			this.startingPlayer = playerA;
			this.playerInTurn = playerA;
		}
		startRematch();
		return true;
	}

	public void setPlayerA(User playerA) {
		this.playerA = playerA;
	}

	public Instant getGameStartedInstant() {
		return gameStartedInstant;
	}

}
