package com.tauhka.games.yatzy;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.spi.DirStateFactory.Result;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.GameResultType;
import com.tauhka.games.core.User;
import com.tauhka.games.core.tables.Table;
import com.tauhka.games.core.tables.TableType;
import com.tauhka.games.core.twodimen.GameResult;
import com.tauhka.games.core.util.Constants;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;

/**
 * @author antsa-1 from GitHub 12 May 2022
 **/

public class YatzyTable extends Table {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(YatzyTable.class.getName());
	private static final Jsonb jsonb = JsonbBuilder.create(); // All the methods in this class are safe for use by multiple concurrent threads.

	@JsonbTransient
	private YatzyRuleBase yatzyRuleBase;
	@JsonbProperty("players")
	private List<YatzyPlayer> players;
	@JsonbProperty("dices")
	private List<Dice> dices;
	@JsonbProperty("timedOutPlayerName")
	private String timedOutPlayerName;

	@JsonbProperty("secondsLeft")
	private int secondsLeft;
	@JsonbProperty("gameResult")
	private GameResultType gameResult;

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
		y.setWebsocketSession(playerA.getWebsocketSession());
		y.setId(playerA.getId());
		y.setTable(this);
		y.setScoreCard(new ScoreCard());
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

	@Override
	public Object playTurn(User user, Object yatzyTurn) {
		YatzyTurn incomingTurn = (YatzyTurn) yatzyTurn;
		LOGGER.info("YatzyTable playTurn rollDices:" + this + "  turn:" + yatzyTurn);
		checkGuards(user);
		return yatzyRuleBase.playTurn(this, incomingTurn);
	}

	public List<Dice> rollDices(User user, List<Dice> dices) {
		if (getPlayerInTurn().getRollsLeft() <= 0) {
			throw new IllegalArgumentException("No rolls left for player:" + user);
		}
		return yatzyRuleBase.rollDices(this, dices, user);
	}

	@Override
	public boolean isGameOver() {
		if (startTime == null) {
			return false;
		}
		// Players can disconnect/leave table anytime. Last player can play the game to the end.
		if (this.players.size() < 1) {
			return true;
		}
		int disabledCount = (int) this.players.stream().filter(player -> !player.isEnabled()).count();
		if (disabledCount == this.players.size()) {
			gameResult = GameResultType.NO_PLAYERS;
			return true;
		}
		int playedAllHands = (int) players.stream().filter(player -> player.getScoreCard().getHands().size() == 15).count();
		return playedAllHands == this.players.size();
	}

	public ScoreCard selectHand(User user, Integer hand) {
		LOGGER.info("YatzyTable playTurn selectHand:" + hand + " table:" + this);
		checkGuards(user);
		if (getPlayerInTurn().getRollsLeft() == 3) {
			throw new IllegalArgumentException("Player has not rolled dices" + user);
		}
		ScoreCard sc = yatzyRuleBase.selectHand(this, user, hand);

		return sc;
	}

	private void checkGuards(User user) {
		if (!getPlayerInTurn().equals(user)) {
			throw new IllegalArgumentException("Player is not in turn:" + user + " table=" + this);
		}
		if (isGameOver()) {
			LOGGER.info("YatzyTable handSelection but gameOver " + this + " ** " + user);
			throw new IllegalArgumentException("Game has ended");
		}

	}

	@Override
	public void onTimeout() {
		timer.cancel();
		System.out.println("YatzyTable TIMEOUT");
		YatzyPlayer playerInTurn = getPlayerInTurn();
		if (playerInTurn != null) {
			playerInTurn.setEnabled(false);
			this.timedOutPlayerName = playerInTurn.getName();
		}
		if (!isGameOver()) {
			playerInTurn = setupNextTurn();
		}

		for (User user : getUsers()) {
			if (user.getWebsocketSession() != null && user.getWebsocketSession().isOpen()) {
				try {
					String table = jsonb.toJson(this);
					String yatzy = null;
					if (gameOver == true) {
						yatzy = ",\"yatzy\":{\"gameOver\":" + isGameOver() + "}, \"result\":" + GameResultType.NO_PLAYERS + "}";
					} else {
						yatzy = ",\"yatzy\":{\"gameOver\":" + isGameOver() + "}}";
					}
					String message = "{\"title\":\"TIMEOUT\", \"table\":" + table + yatzy;
					user.getWebsocketSession().getBasicRemote().sendText(message);
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, "CommonEndpoint sendMessage to table io.error", e);
				}
			}
		}
		timedOutPlayerName = null;
	}

	public String getTimedOutPlayerName() {
		return timedOutPlayerName;
	}

	public void setTimedOutPlayerName(String timedOutPlayerName) {
		this.timedOutPlayerName = timedOutPlayerName;
	}

	public List<YatzyPlayer> getPlayers() {
		return players;
	}

	public List<Dice> getDices() {
		return dices;
	}

	public void setDices(List<Dice> dices) {
		this.dices = dices;
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
	public boolean isPlayer(User user) {
		return players.indexOf(user) != -1;
	}

	@Override
	public boolean isInTable(User user) {
		if (isPlayer(user)) {
			return true;
		}
		return super.isWatcher(user);
	}

	@JsonbTransient
	@Override
	public List<User> getUsers() {
		List<User> users = new ArrayList<User>();
		users.addAll(players);
		users.addAll(watchers);
		return users;
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
		y.setScoreCard(new ScoreCard());
		y.setWebsocketSession(user.getWebsocketSession());
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
		yatzyRuleBase.startGame(this);
		return gameShouldStartNow;
	}

	@Override
	public boolean removePlayerIfExist(User user) {
		YatzyPlayer removedPlayer = null;
		int index = players.indexOf(user);
		if (playerA != null && playerA.equals(user)) {
			detachPlayer(playerA);
//			removedPlayer = playerA;
			// playerA = null;
		}
		if (index != -1) {
			// removedPlayer = players.remove(index);
			removedPlayer = players.get(index);
			removedPlayer.setEnabled(false);
			detachPlayer(removedPlayer);
		}
		if (removedPlayer == null) {
			// Nobody was removed
			return false;
		}
		if (playerInTurn.equals(removedPlayer)) {
			playerInTurn = setupNextTurn();
		}
		return true;
	}

	@Override
	public void changePlayerInTurn() {
		playerInTurn = setupNextTurn();
	}

	private YatzyPlayer setupNextTurn() {
		int currentPlayerIndex = players.indexOf(playerInTurn);
		int breakoutCondition = 0;
		while (breakoutCondition < 4) {
			currentPlayerIndex++;
			if (currentPlayerIndex >= players.size()) {
				currentPlayerIndex = 0;
			}
			YatzyPlayer y = players.get(currentPlayerIndex);
			if (y.isEnabled()) {
				y.setRollsLeft(3);
				startTimer();
				playerInTurn = y;
				return y;
			}
			breakoutCondition++;
		}
		gameOver = true;
		return null;
	}

	/* private void determineNextPlayerInTurn(int removedIndex) { if (players.size() == 0) { playerInTurn = null; return; } if (removedIndex == players.size() - 1) { playerInTurn = players.get(0); } else { playerInTurn =
	 * players.get(removedIndex); }
	 * 
	 * } */
	@Override
	public YatzyPlayer getPlayerInTurn() {
		return (YatzyPlayer) playerInTurn;
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

	@Override
	public String toString() {
		return "YatzyTable [timer=" + timer + ", players=" + players + ", dices=" + dices + "]";
	}

}
