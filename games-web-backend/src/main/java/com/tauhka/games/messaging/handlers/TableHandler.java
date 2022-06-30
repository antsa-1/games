package com.tauhka.games.messaging.handlers;

import static com.tauhka.games.core.util.Constants.SYSTEM;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.User;
import com.tauhka.games.core.ai.AI;
import com.tauhka.games.core.tables.ConnectFourTable;
import com.tauhka.games.core.tables.Table;
import com.tauhka.games.core.tables.TableType;
import com.tauhka.games.core.tables.TicTacToeTable;
import com.tauhka.games.core.twodimen.GameResult;
import com.tauhka.games.messaging.Message;
import com.tauhka.games.messaging.MessageTitle;
import com.tauhka.games.pool.PoolTable;
import com.tauhka.games.web.websocket.CommonEndpoint;
import com.tauhka.games.yatzy.YatzyTable;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;

@Default
@Dependent
public class TableHandler extends CommonHandler {
	private static final Logger LOGGER = Logger.getLogger(TableHandler.class.getName());

	@Inject
	private AIHandler aiHandler;

	public Message createTable(Message message, CommonEndpoint endpoint) {
		Stream<Table> stream = CommonEndpoint.TABLES.values().stream();
		stream = stream.filter(table -> table.isInTable(endpoint.getUser()));
		Optional<Table> tableOptional = stream.findFirst();
		if (tableOptional.isPresent()) {
			throw new IllegalArgumentException("User is already in table:" + endpoint.getUser() + " table:" + tableOptional.get());
		}

		Table table = null;

		GameMode gameMode = GameMode.getGameMode(Integer.parseInt(message.getMessage()));
		if (GameMode.CONNECT4 == gameMode.getGameNumber()) {
			table = new ConnectFourTable(endpoint.getUser(), gameMode, false, message.getOnlyRegistered(), message.getTimeControlIndex()); // many times same parameters.. one object would be better, next version?
		} else if (GameMode.POOL == gameMode.getGameNumber()) {
			PoolTable p = new PoolTable(endpoint.getUser(), gameMode, message.getRandomStarter(), message.getOnlyRegistered(), message.getTimeControlIndex());
			table = p;
		} else if (GameMode.YATZY == gameMode.getGameNumber()) {
			YatzyTable y = new YatzyTable(endpoint.getUser(), gameMode, message.getRandomStarter(), message.getOnlyRegistered(), message.getTimeControlIndex(), message.getPlayerAmount());
			table = y;
		} else {
			table = new TicTacToeTable(endpoint.getUser(), gameMode, false, message.getOnlyRegistered(), message.getTimeControlIndex());
		}
		endpoint.getUser().setTable(table);
		CommonEndpoint.TABLES.put(table.getTableId(), table);
		Message message_ = null;
		if (message.getComputer()) {
			User user = aiHandler.createAIPlayer(gameMode);
			if (table.joinTableAsPlayer(user)) {
				message_ = new Message();
				message_.setTitle(MessageTitle.START_GAME);
				message_.setTable(table);
			}
		} else {
			message_ = new Message();
			message_.setTitle(MessageTitle.CREATE_TABLE);
			message_.setTable(table);
		}
		return message_;
	}

	public Message leaveTable(Message mes, CommonEndpoint endpoint) {
		UUID tableId = UUID.fromString(mes.getMessage());
		Table table = CommonEndpoint.TABLES.get(tableId);
		if (table == null) {
			return null;
		}
		User user = endpoint.getUser();
		user.setTable(null);
		if (table.isPlayer(user) && table.getStartTime() != null && table.getTableType() == TableType.MULTI) {
			table.leaveTable(user);
		} else if (table.isPlayer(user) && table.getStartTime() != null) {
			handleLeavingPlayerStatistics(table, endpoint);
		}
		Message message = new Message();
		message.setTitle(MessageTitle.LEAVE_TABLE);
		message.setTable(table);
		if (table.removePlayerIfExist(user) || table.removeWatcherIfExist(user)) {
			message.setWho(endpoint.getUser());
			return message;
		}
		return null;
	}

	public Message removeTableIfRequired(Table table, CommonEndpoint endpoint) {
		if (table == null) {
			return null;
		}

		if (table.isStarted() && !table.isSomebodyInTurn() || !table.isStarted() && table.getPlayerA() == null) {
			CommonEndpoint.TABLES.remove(table.getTableId());
			table.detachPlayers();
			Message message = new Message();
			message.setTitle(MessageTitle.REMOVE_TABLE);
			message.setTable(table);
			return message;
		}
		return null;
	}

	public Message removeEndpointOwnTable(CommonEndpoint endpoint) {
		// Using params would be faster...
		Optional<Table> tableOptional = CommonEndpoint.TABLES.values().stream().filter(taabel -> taabel.getPlayerA() != null).filter(taabel -> taabel.getPlayerA().equals(endpoint.getUser())).findFirst();
		if (tableOptional.isPresent()) {
			tableOptional.get().detachPlayers();
			endpoint.getUser().setTable(null);
			Table removedTable = CommonEndpoint.TABLES.remove(tableOptional.get().getTableId());
			Message message_ = new Message();
			message_.setTitle(MessageTitle.REMOVE_TABLE);
			message_.setTable(removedTable);
			return message_;
		}
		throw new IllegalArgumentException("No playerA found in tables:" + endpoint.getUser());
	}

	public Message joinTable(Message message, CommonEndpoint endpoint) {
		if (endpoint.getUser().getTable() != null) {
			LOGGER.severe("User is already in table:" + endpoint.getUser());
			throw new IllegalArgumentException("User is already in table:" + endpoint.getUser());
		}
		UUID tableID = UUID.fromString(message.getMessage());
		Table table = CommonEndpoint.TABLES.get(tableID);
		synchronized (table) {
			if (!table.isWaitingOpponent()) {
				throw new IllegalArgumentException("Table is not waiting player " + table + " trying user:" + endpoint.getUser());
			}
			if (table.isInTable(endpoint.getUser())) {
				throw new IllegalArgumentException("Same players to table not possible..");
			}
			User user = endpoint.getUser();
			user.setTable(table);
			Message message_ = new Message();
			message_.setWho(endpoint.getUser());
			if (table.joinTableAsPlayer(user)) {
				message_.setTitle(MessageTitle.START_GAME);
			} else {
				message_.setTitle(MessageTitle.JOIN_TABLE);
			}
			message_.setTable(table);
			return message_;
		}

	}

	public Message watch(Message message, CommonEndpoint CommonEndpoint) {
		UUID tableID = UUID.fromString(message.getMessage());
		Table table = CommonEndpoint.TABLES.get(tableID);
		if (table.addWatcher(CommonEndpoint.getUser())) {
			Message message_ = new Message();
			message_.setTitle(MessageTitle.WATCH);
			message_.setTable(table);
			return message_;
		}
		throw new IllegalArgumentException("Watcher not allowed in table" + table);
	}

	public Message getWatcherInfo(Message message, CommonEndpoint CommonEndpoint) {
		UUID tableID = UUID.fromString(message.getMessage());
		Table table = CommonEndpoint.TABLES.get(tableID);
		Message watcherMessage = new Message();
		watcherMessage.setTitle(MessageTitle.ADD_WATCHER);
		watcherMessage.setWho(CommonEndpoint.getUser());
		watcherMessage.setTable(table);
		return watcherMessage;
	}

	public Message resign(CommonEndpoint endpoint) {

		Stream<Table> stream = CommonEndpoint.TABLES.values().stream();
		stream = stream.filter(table -> table.isPlayer(endpoint.getUser()));
		Optional<Table> tableOptional = stream.findFirst();
		if (tableOptional.isEmpty()) {
			throw new IllegalArgumentException("resign not possible, no table for:" + endpoint.getUser());
		}
		Table table = tableOptional.get();
		GameResult gameResult = table.resign(endpoint.getUser());
		// For Artificial player set Rematch-state ready
		if (table.getOpponent(endpoint.getUser()) instanceof AI) {
			table.suggestRematch(table.getOpponent(endpoint.getUser()));
		}
		Message resultMessage = new Message();
		resultMessage.setFrom(SYSTEM);
		resultMessage.setTable(table);
		if (table.isGameOver()) {
			resultMessage.setGameResult(gameResult);
		}
		resultMessage.setWho(endpoint.getUser());
		resultMessage.setTitle(MessageTitle.RESIGN);
		return resultMessage;

	}

	public Message rematch(CommonEndpoint endpoint) {
		// Using params would be faster...
		Stream<Table> stream = CommonEndpoint.TABLES.values().stream();
		stream = stream.filter(table -> table.isPlayer(endpoint.getUser()));
		Optional<Table> tableOptional = stream.findFirst();
		if (tableOptional.isEmpty()) {
			throw new IllegalArgumentException("rematch not possible, no table for:" + endpoint.getUser());

		}
		Table table = tableOptional.get();
		boolean startRematch = table.suggestRematch(endpoint.getUser());
		if (startRematch) {
			Message rematchMessage = new Message();
			rematchMessage.setFrom("System");
			rematchMessage.setTable(table);

			rematchMessage.setTitle(MessageTitle.REMATCH);
			return rematchMessage;
		}
		return null;
	}

}
