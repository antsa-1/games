package com.tauhka.games.core.tables;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.User;
import com.tauhka.games.core.ai.AI;
import com.tauhka.games.core.twodimen.GameResult;
import com.tauhka.games.core.util.Constants;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;

/**
 * @author antsa-1 from GitHub 12 May 2022
 **/

public abstract class MultiplayerTable extends Table // extends Table
{
	private static final long serialVersionUID = 1L;

	@JsonbProperty("players")
	private List<User> players;

	public MultiplayerTable(User playerA, GameMode gameMode, boolean randomizeStarter, boolean registeredOnly, int timeControlIndex, int playerAmount) {
		super(playerA, gameMode, randomizeStarter, registeredOnly, timeControlIndex, playerAmount);
		// this.tableId = UUID.randomUUID();
		if (playerAmount < 2 || playerAmount > 4) {
			throw new IllegalArgumentException("incorrect amount of players" + playerAmount);
		}
		this.players = new ArrayList<User>(playerAmount);
		this.gameMode = gameMode;
		this.randomizeStarter = randomizeStarter;
		if (!randomizeStarter) {
			this.playerInTurn = playerA;
		}
		this.players.add(playerA);
		this.registeredOnly = registeredOnly;
//		this.timeControlIndex = timeControlIndex;
	}

	public abstract GameResult checkWinAndDraw();

	public abstract Object playTurn(User user, Object o);

	protected abstract Table startRematch();

	@JsonbTransient
	public boolean isArtificialPlayerInTurn() {
		return this.playerInTurn != null && this.playerInTurn instanceof AI;
	}

	@JsonbTransient
	public boolean isWaitingOpponent() {
		return this.players.size() < playerAmount;
	}

	public boolean isPlayerInTurn(User u) {
		if (this.playerInTurn == null) {
			return false;
		}
		return this.playerInTurn.equals(u);
	}

	/* public synchronized GameResult resign(User player) { if (this.isPlayer(player) && this.playerInTurn != null) { this.playerInTurn = null; this.gameOver = true; User opponent = getOpponent(player); GameResult gameResult = new
	 * GameResult(); gameResult.setStartInstant(this.gameStartedInstant); gameResult.setWinner(opponent); gameResult.setPlayerA(this.playerA); gameResult.setPlayerB(this.playerB); gameResult.setResigner(player);
	 * gameResult.setResultType(GameResultType.WIN_BY_RESIGNATION); return gameResult; } throw new IllegalArgumentException("Resign not possible" + player); } */

	public boolean isGameOver() {
		return gameOver;
	}

	public void setGameOver(boolean gameOver) {
		this.gameOver = gameOver;
	}

	public GameMode getGameMode() {
		return gameMode;
	}

	public int getTimeControlIndex() {
		return timeControlIndex;
	}

	public void setTimeControlIndex(int timeControlIndex) {
		this.timeControlIndex = timeControlIndex;
	}

	/* public boolean isPlayer(User user) { if (user == null) { return false; } return user.equals(playerA) || user.equals(playerB); } */
	public boolean isWatcher(User user) {
		return this.watchers.contains(user);
	}

	public boolean addWatcher(User watcher) {
		// TODO if at least two players let
		if (!this.watchers.contains(watcher)) {
			this.watchers.add(watcher);
			return true;
		}
		return false;
	}

	/* public void initRematchForArtificialPlayer() { // PlayerA can't be Artificial player since it does not create tables if (this.players.get(1) != null && this.players.get(1) instanceof AI) { this.suggestRematch(this.players.get(1)); }
	 * } */
	public int getTimer() {
		return timer;
	}

	public void setTimer(int timer) {
		this.timer = timer;
	}

	@JsonbTransient
	public List<User> getUsers() {
		List<User> users = new ArrayList<User>();
		/* if (playerA != null) { users.add(playerA); } if (playerB != null) { users.add(playerB); } */
		users.addAll(watchers);
		return users;
	}

	/* public boolean removePlayerIfExist(User user) { if (playerA != null && playerA.equals(user)) { playerA = null; this.playerInTurn = null; return true; } else if (playerB != null && playerB.equals(user)) { playerB = null;
	 * this.playerInTurn = null; return true; } return false; } */

	public boolean removeWatcherIfExist(User user) {
		return watchers.remove(user);
	}

	public User getPlayerInTurn() {
		return playerInTurn;
	}

	public void changePlayerInTurn() {

	}

	@Override
	public synchronized boolean joinTableAsPlayer(User joiningPlayer) {
		if (this.registeredOnly && joiningPlayer.getName().startsWith(Constants.GUEST_LOGIN_NAME)) {
			// Also guest players can use this option atm..
			throw new IllegalArgumentException("Only registered players allowed to join to play." + joiningPlayer.getName() + " table:" + this);
		}
		this.players.add(joiningPlayer);
		if (players.size() != playerAmount) {
			return false;
		}
		// Table full
		this.gameStartedInstant = Instant.now();
		if (this.randomizeStarter) {
			int i = ThreadLocalRandom.current().nextInt(1, players.size());
			this.startingPlayer = players.get(i - 1);
			playerInTurn = this.startingPlayer;

		} else {
			this.startingPlayer = players.get(0);
			playerInTurn = players.get(0);
		}
		return gameShouldStartNow;
	}

	public List<User> getWatchers() {
		return watchers;
	}

	public boolean isRegisteredOnly() {
		return registeredOnly;
	}
	/* public synchronized boolean suggestRematch(User user) { if (!isPlayer(user)) { throw new IllegalArgumentException("Rematchplayer is not a player in the table:" + user); } if (this.rematchPlayer == null) { this.rematchPlayer = user;
	 * return false; } if (this.rematchPlayer.equals(user)) { return false; } if (this.startingPlayer.equals(playerA)) { this.startingPlayer = playerB; this.playerInTurn = playerB; } else { this.startingPlayer = playerA; this.playerInTurn =
	 * playerA; } startRematch(); return true; } */

	public List<User> getPlayers() {
		return players;
	}

	public void setPlayers(List<User> players) {
		this.players = players;
	}

}
