package com.tauhka.games.yatzy;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.User;
import com.tauhka.games.core.events.AITurnEvent;
import com.tauhka.games.core.stats.Player;
import com.tauhka.games.core.stats.Status;
import com.tauhka.games.core.tables.Table;
import com.tauhka.games.core.tables.TableType;
import com.tauhka.games.core.timer.TimeControlIndex;
import com.tauhka.games.core.twodimen.GameResult;
import com.tauhka.games.core.util.Constants;
import com.tauhka.games.yatzy.util.YatzyGameEJB;

import jakarta.enterprise.inject.spi.CDI;
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
	private static final Jsonb jsonb = JsonbBuilder.create(); // All the methods in Jsonb class are safe for use by multiple concurrent threads.

	@JsonbTransient
	private YatzyRuleBase yatzyRuleBase;
	@JsonbProperty("players")
	private List<YatzyPlayer> players;
	@JsonbProperty("dices")
	private List<Dice> dices;
	@JsonbProperty("timedOutPlayerName")
	private String timedOutPlayerName;
	@JsonbTransient
	private UUID gameId;
	@JsonbTransient
	private Set<YatzyPlayer> rematchPlayers;
	@JsonbProperty("secondsLeft")
	private int secondsLeft;

	public YatzyTable(User playerA, GameMode gameMode, boolean randomizeStarter, boolean registeredOnly, int timeControlIndex, int playerAmount) {
		super(gameMode, randomizeStarter, registeredOnly, timeControlIndex, playerAmount);
		if (playerAmount < 2 || playerAmount > 4) {
			throw new IllegalArgumentException("incorrect amount of players" + playerAmount);
		}
		if (gameMode.getPredifinedTimeControl() != null) {
			super.timeControlIndex = gameMode.getPredifinedTimeControl().getIndex();
		} else if (gameMode.getId() == 40 && timeControlIndex < 3) {
			super.timeControlIndex = 5;// classic yatzy default time control in case UI does not send the parameter
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
		// Not used
		throw new IllegalAccessError("YatzyTable playTurn");
	}

	public boolean getRandomizeStarter() {
		return super.randomizeStarter;
	}

	public List<Dice> rollDices(User user, List<Dice> dices) {
		LOGGER.info("YatzyTable rollDices from user:" + user + " table:" + this);
		if (getPlayerInTurn().getRollsLeft() == 3) {
			this.getDices().forEach(dice -> dice.setSelected(false));
		}
		if (getPlayerInTurn().getRollsLeft() <= 0) {
			throw new IllegalArgumentException("No rolls left for player:" + user);
		}
		List<Dice> dicesRolled = yatzyRuleBase.rollDices(this, dices, user);

		return dicesRolled;
	}

	public Integer getPosition(Dice dice) {
		for (int i = 0; i < this.dices.size(); i++) {
			if (dice.getDiceId().equals(this.dices.get(i).getDiceId())) {
				return i + 1;
			}
		}
		return null;
	}

	public void setStartTime(Instant startTime) {
		super.startTime = startTime;
	}

	@Override
	public boolean isWaitingOpponent() {
		return this.players.size() != this.playerAmount && !isStarted();
	}

	@Override
	public boolean isGameOver() {
		if (startTime == null) {
			return false;
		}
		if (gameOver && gameResult.isComplete()) {
			return true;
		}
		// Players can disconnect/leave table anytime. Last player can play the game to the end. if (this.players.size() < 1) { gameOver = true; }
		List<YatzyPlayer> enabledPlayers = this.players.stream().filter(player -> player.isEnabled()).collect(Collectors.toList());
		if (enabledPlayers.size() == 1) {
			if (enabledPlayers.get(0) instanceof YatzyAI) {
				gameOver = true;
			}
		}
		if (!gameOver && this.players.size() - enabledPlayers.size() == this.players.size()) {
			gameOver = true;
		}
		if (!gameOver && hasEnabledPlayersPlayedAllHands(enabledPlayers)) {
			gameOver = true;
		}
		return gameOver;
	}

	public void onGameOver() {
		cancelTimer();
		if (!gameResult.isComplete()) {
			finalizeGameResult();
			YatzyGameEJB yatzyGameEjb = CDI.current().select(YatzyGameEJB.class).get();
			yatzyGameEjb.saveYatzyGame(gameResult);
		}
		for (YatzyPlayer y : players) {
			if (y.isComputerPlayer()) {
				suggestRematch(y);
			}
		}
		playerInTurn = null;
	}

	private void finalizeGameResult() {
		gameResult.setStartInstant(this.startTime);
		gameResult.setEndInstant(Instant.now());
		gameResult.setGameId(gameId);
		gameResult.setGameMode(this.gameMode);
		gameResult.setTimeControlIndex(TimeControlIndex.getWithIndex(timeControlIndex));
		setFinishStatuses();
		setFinishPositions();
	}

	private void setFinishPositions() {
		Collections.sort(gameResult.getPlayers());
		for (int i = 0; i < gameResult.getPlayers().size(); i++) {
			Player indexPlayer = gameResult.getPlayers().get(i);
			if (indexPlayer.getFinishPosition() == null) {
				indexPlayer.setFinishPosition(i);
			}
			for (int j = i; j < gameResult.getPlayers().size(); j++) {
				Player nextPlayer = gameResult.getPlayers().get(j);
				if (nextPlayer.getScore() == indexPlayer.getScore() && nextPlayer.getFinishPosition() == null) {
					nextPlayer.setFinishPosition(i);
				}
			}
		}
	}

	private void setFinishStatuses() {
		for (YatzyPlayer y : players) {
			Player p = gameResult.findPlayer(y.getName());
			p.setScore(y.getScoreCard().calculateTotal());
			if (p.getStatus() == null) {
				p.setStatus(Status.FINISHED);
			}
		}
	}

	private boolean hasEnabledPlayersPlayedAllHands(List<YatzyPlayer> enabledPlayers) {
		return (int) enabledPlayers.stream().filter(player -> player.getScoreCard().getHands().size() == 15).count() == enabledPlayers.size();
	}

	public ScoreCard selectHand(User user, Integer hand) {
		LOGGER.info("YatzyTable selectHand from user:" + user + " selectHand:" + hand + " table:" + this);
		checkGuards(user);
		if (getPlayerInTurn().getRollsLeft() == 3) {
			throw new IllegalArgumentException("Player has not rolled dices" + user);
		}
		return yatzyRuleBase.selectHand(this, user, hand);
	}

	private void checkGuards(User user) {
		if (!getPlayerInTurn().equals(user)) {
			throw new IllegalArgumentException("Player is not in turn:" + user + " table=" + this);
		}
		if (isGameOver()) {
			onGameOver();
			LOGGER.info("YatzyTable handSelection but gameOver " + this + " ** " + user);
			throw new IllegalArgumentException("Game has ended");
		}

	}

	@Override
	public void onTimeout(User timedOut) {
		synchronized (this) {
			System.out.println("YatzyTable TIMEOUT+" + getPlayerInTurn());
			cancelTimer();
			YatzyPlayer playerInTurn = getPlayerInTurn();
			if (playerInTurn != null) {
				playerInTurn.setEnabled(false);
				timedOutPlayerName = playerInTurn.getName();
				// Guest players don't have id
				getGameResult().changeStatus(playerInTurn.getName(), playerInTurn.getId(), Status.TIMED_OUT);
			}
			setupNextTurn();
			if (isGameOver()) {
				onGameOver();
			}
			String tableJson = jsonb.toJson(this);
			String message = "{\"title\":\"TIMEOUT\", \"table\":" + tableJson + "}";
			sendMessageToTable(message);
			timedOutPlayerName = null;
			if (isArtificialPlayerInTurn()) {
				CDI.current().getBeanManager().getEvent().fireAsync(new AITurnEvent(this));
			}
		}
	}

	private void sendMessageToTable(String message) {
		synchronized (this) {
			for (User user : getUsers()) {
				if (user.getWebsocketSession() != null && user.getWebsocketSession().isOpen()) {
					try {
						user.getWebsocketSession().getBasicRemote().sendText(message);
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, "YatzyTable timeout message to table failed", e);
					}
				}
			}
		}
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

		return null;
	}

	public UUID getGameId() {
		return gameId;
	}

	public void setGameId(UUID gameId) {
		this.gameId = gameId;
	}

	@Override
	public void onClose() {
		LOGGER.info("YatzyTable onClose" + this);
		super.onClose();
		players = null;
		timedOutPlayerName = null;
		dices = null;
		yatzyRuleBase = null;
		rematchPlayers = null;
	}

	@Override
	public void detachPlayers() {
		super.detachPlayers();
		for (YatzyPlayer u : this.players) {
			u.setTable(null);
			u.setWebsocketSession(null);
			u.setId(null);
			u.setScoreCard(null);
			u.setName(null);
		}
	}

	@Override
	public boolean isPlayer(User user) {
		int index = players.indexOf(user);
		if (index < 0) {
			return false;
		}
		return true;
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

	public int getEnabledPlayersCount() {
		return (int) this.players.stream().filter(player -> player.isEnabled()).count();
	}

	@Override
	public boolean joinTableAsPlayer(User user) {

		if (this.registeredOnly && user.getName().startsWith(Constants.GUEST_LOGIN_NAME)) {
			// Also guest players can use this option atm..
			throw new IllegalArgumentException("Only registered players allowed to join to play." + user.getName() + " table:" + this);
		}
		YatzyPlayer y = user.getName().equals(Constants.OLAV_COMPUTER) ? new YatzyAI() : new YatzyPlayer(); // instanceof AI
		y.setName(user.getName());
		y.setId(user.getId());
		y.setScoreCard(new ScoreCard());
		y.setWebsocketSession(user.getWebsocketSession());
		this.players.add(y);
		if (players.size() != playerAmount) {
			return false;
		}
		// Table full
		yatzyRuleBase = new YatzyRuleBase();
		yatzyRuleBase.startGame(this);
		return gameShouldStartNow;
	}

	@Override
	public void changePlayerInTurn() {
		playerInTurn = setupNextTurn();
	}

	private YatzyPlayer setupNextTurn() {
		cancelTimer();
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
				getDices().forEach(dice -> dice.unselect());
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

	public void cancelTimer() {
		if (this.timer != null)
			timer.cancel();
	}

	@Override
	protected Table startRematch() {
		this.yatzyRuleBase.startGame(this);
		return this;
	}

	@JsonbProperty(value = "tableType")
	public TableType getTableType() {
		return TableType.MULTI;
	}

	@Override
	public String toString() {
		return "YatzyTable [players=" + players + ", dices=" + dices + ", timedOutPlayerName=" + timedOutPlayerName + ", gameId=" + gameId + ", secondsLeft=" + secondsLeft + ", playerInTurn=" + playerInTurn + "]";
	}

	@Override
	public boolean addWatcher(User watcher) {
		if (this.startTime == null && this.playerA == null) {
			return false;
		}
		if (!this.watchers.contains(watcher)) {
			this.watchers.add(watcher);
			return true;
		}
		return false;
	}

	@Override
	public boolean removePlayerIfExist(User user) {
		// leaveTable handles basically this
		return false;
	}

	@Override
	public void leaveTable(User user) {
		if (isWatcher(user)) {
			watchers.remove(user);
			return;
		}
		synchronized (this) {
			int playerIndex = players.indexOf(user);
			if (playerIndex >= 0) {
				YatzyPlayer y = players.get(playerIndex);
				y.setEnabled(false);
				updateResult(y);
				user.setTable(null);
				players.remove(user);
				if (playerA != null && user.equals(playerA)) {
					playerA = null;
				}
			}
			if (isGameOver()) {
				onGameOver();
				onClose();// Last player left -> close table. Only computer might sit in this table
				return;
			}
			if (playerInTurn != null && playerInTurn.equals(user)) {
				setupNextTurn();
			}
			if (isArtificialPlayerInTurn()) {
				CDI.current().getBeanManager().getEvent().fireAsync(new AITurnEvent(this));
			}
		}
	}

	@Override
	public GameResult resign(User player) {
		synchronized (this) {
			if (this.isPlayer(player) && this.playerInTurn != null) {
				int index = players.indexOf(player);
				if (index < 0) {
					LOGGER.info("YatzyTable: no resign player found");
					return null;
				}
				players.get(index).setEnabled(false);
				gameResult.findPlayer(player.getName()).setStatus(Status.RESIGNED);
				if (playerInTurn.equals(player)) {
					setupNextTurn();
				}
				if (isArtificialPlayerInTurn()) {
					CDI.current().getBeanManager().getEvent().fireAsync(new AITurnEvent(this));
				}
				return null;
			}
			throw new IllegalArgumentException("Resign not possible" + player);
		}
	}

	@Override
	public boolean suggestRematch(User user) {
		if (!isPlayer(user)) {
			return false;
		}
		synchronized (this) {
			int index = players.indexOf(user);
			if (index == -1) {
				LOGGER.info("Yatzy wrong rematch player, not in the game:" + user);
				return false;
			}
			rematchPlayers.add(players.get(index));
			if (rematchPlayers.size() == players.size() && rematchPlayers.size() != 1) {
				// All players required to click "rematch"
				yatzyRuleBase.startGame(this);
				return true;
			}
			return false;
		}
	}

	public Set<YatzyPlayer> getRematchPlayers() {
		return rematchPlayers;
	}

	private void updateResult(YatzyPlayer y) {
		if (!this.isStarted()) {
			return;
		}
		if (y.isGuestPlayer()) {
			getGameResult().findPlayer(y.getName()).setStatus(Status.LEFT);
		} else {
			getGameResult().findPlayer(y.getId()).setStatus(Status.LEFT);
		}
	}

	public void resetRematchPlayers() {
		this.rematchPlayers = new HashSet<YatzyPlayer>();
	}

}
