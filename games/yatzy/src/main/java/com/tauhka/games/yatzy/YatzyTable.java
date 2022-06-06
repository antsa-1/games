package com.tauhka.games.yatzy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ThreadLocalRandom;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.User;
import com.tauhka.games.core.tables.Table;
import com.tauhka.games.core.tables.TableType;
import com.tauhka.games.core.twodimen.GameResult;
import com.tauhka.games.core.util.Constants;

import jakarta.json.bind.annotation.JsonbProperty;

/**
 * @author antsa-1 from GitHub 12 May 2022
 **/

public class YatzyTable extends Table {
	private static final long serialVersionUID = 1L;
	private Timer timer;
	private YatzyRuleBase yatzyRuleBase;
	@JsonbProperty("players")
	private List<YatzyPlayer> players;

	public YatzyTable(User playerA, GameMode gameMode, boolean randomizeStarter, boolean registeredOnly, int timeControlIndex, int playerAmount) {
		super(gameMode, randomizeStarter, registeredOnly, timeControlIndex, playerAmount);
		if (playerAmount < 2 || playerAmount > 4) {
			throw new IllegalArgumentException("incorrect amount of players" + playerAmount);
		}
		this.players = new ArrayList<YatzyPlayer>(playerAmount);
		this.gameMode = gameMode;
		this.randomizeStarter = randomizeStarter;
		YatzyPlayer y = new YatzyPlayer();
		y.setName(playerA.getName());
		y.setId(playerA.getId());
		y.setTable(this);
		super.setPlayerA(y);
		if (!randomizeStarter) {
			this.playerInTurn = y;
		}
		this.players.add(y);
		this.registeredOnly = registeredOnly;

	}

	public YatzyTable() {
		// Empty constr for deserialization

	}

	public List<YatzyPlayer> getPlayers() {
		return players;
	}

	@Override
	public GameResult checkWinAndDraw() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void detachPlayers() {
		super.detachPlayers();
		for (User u : this.players) {
			u.setTable(null);
		}
	}

	@Override
	public boolean isInTable(User user) {
		if (isPlayer(user)) {
			return true;
		}
		return super.isWatcher(user);
	}

	@Override
	public boolean joinTableAsPlayer(User user) {

		if (this.registeredOnly && user.getName().startsWith(Constants.GUEST_LOGIN_NAME)) {
			// Also guest players can use this option atm..
			throw new IllegalArgumentException("Only registered players allowed to join to play." + user.getName() + " table:" + this);
		}
		YatzyPlayer y = new YatzyPlayer();
		y.setName(user.getName());
		y.setId(user.getId());
		this.players.add(y);
		if (players.size() != playerAmount) {
			return false;
		}
		// Table full
		this.startTime = Instant.now();
		if (this.randomizeStarter) {
			int i = ThreadLocalRandom.current().nextInt(1, players.size());
			this.startingPlayer = players.get(i - 1);
			playerInTurn = this.startingPlayer;

		} else {
			this.startingPlayer = players.get(0);
			playerInTurn = players.get(0);
		}
		yatzyRuleBase = new YatzyRuleBase();
		YatzyPlayer yPlayer = (YatzyPlayer) super.getPlayerInTurn();
		yatzyRuleBase.startGame(yPlayer);
		return gameShouldStartNow;
	}

	@Override
	public YatzyPlayer getPlayerInTurn() {
		return (YatzyPlayer) playerInTurn;
	}

	@Override
	public Object playTurn(User user, Object yatzyTurn) {
		if (playerInTurn.equals(user)) {
			throw new IllegalArgumentException("Player is not inTurn " + user);
		}
		YatzyTurn turn = (YatzyTurn) yatzyTurn;
		return yatzyRuleBase.playTurn(this, turn);
	}

	@Override
	protected Table startRematch() {
		// TODO Auto-generated method stub
		return null;
	}

	@JsonbProperty(value = "tableType")
	public TableType getTableType() {
		return TableType.MULTI;
	}

}
