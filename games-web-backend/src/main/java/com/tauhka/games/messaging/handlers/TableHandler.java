package com.tauhka.games.messaging.handlers;

import static com.tauhka.games.core.util.Constants.SYSTEM;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.User;
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
				return message_;
			}
		}
		message_ = new Message();
		message_.setTitle(MessageTitle.CREATE_TABLE);
		message_.setTable(table);

		return message_;
	}

	public Message leaveTable(Message mes, CommonEndpoint endpoint) {
		UUID tableId = UUID.fromString(mes.getMessage());
		User user = endpoint.getUser();
		user.setTable(null);
		Table table = CommonEndpoint.TABLES.get(tableId);
		if (table == null) {
			return null;
		}

		if (!table.isInTable(user)) {
			return null;
		}
		table.leaveTable(user);
		Message message = new Message();
		message.setTitle(MessageTitle.LEAVE_TABLE);
		message.setTable(table);
		message.setWho(endpoint.getUser());
		return message;
////			handleLeavingPlayerStatistics(table, endpoint); in to table
	}

	public Message removeTable(Table table, User user) {
		CommonEndpoint.TABLES.remove(table.getTableId());
		String tableId = table.getTableId().toString();
		Message message = new Message();
		message.setTitle(MessageTitle.REMOVE_TABLE);
		message.setMessage(tableId);
		if (user != null)
			message.setWho(user);
		return message;
	}

	public Message removeEndpointOwnTable(CommonEndpoint endpoint) {
		Table table = endpoint.getUser().getTable();
		if (table == null) {
			throw new IllegalArgumentException("removeEndpointOwnTable User is not in any table, cannot remove table" + endpoint.getUser());
		}
		if (table.getPlayerA() == null || !table.getPlayerA().equals(table.getPlayerA())) {
			throw new IllegalArgumentException("removeEndpointOwnTable User is not creator of the table " + table + " who:" + endpoint.getUser());
		}
//		if (!table.isRemovable()) {
//			throw new IllegalArgumentException("removeEndpointOwnTable table not removable at the moment");
//		}
		Table removedTable = CommonEndpoint.TABLES.remove(table.getTableId());
		String tableId = removedTable.getTableId().toString();
		endpoint.getUser().setTable(null);
		removedTable.onClose();
		Message message_ = new Message();
		message_.setTitle(MessageTitle.REMOVE_TABLE);
		message_.setMessage(tableId);
		return message_;
	}

	public Message joinTable(Message message, CommonEndpoint endpoint) {
		if (endpoint.getUser().getTable() != null) {
			LOGGER.severe("User is already in table:" + endpoint.getUser());
			throw new IllegalArgumentException("User is already in table:" + endpoint.getUser() + " table:" + endpoint.getUser().getTable());
		}
		UUID tableID = UUID.fromString(message.getMessage());
		Table table = CommonEndpoint.TABLES.get(tableID);
		if (table == null) {
			LOGGER.info("No Such Table:" + tableID);
			return null;
		}
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

	public Message watch(Message message, CommonEndpoint commonEndpoint) {
		UUID tableID = UUID.fromString(message.getMessage());
		Table table = commonEndpoint.TABLES.get(tableID);
		if (table == null) {
			LOGGER.info("No Such Table to watch:" + tableID);
		}
		if (table.addWatcher(commonEndpoint.getUser())) {
			Message message_ = new Message();
			message_.setTitle(MessageTitle.WATCH);
			commonEndpoint.getUser().setTable(table);
			message_.setTable(table);
			return message_;
		}
		throw new IllegalArgumentException("Watcher not allowed in table" + table);
	}

	public Message createWatchMessage(Message message, CommonEndpoint CommonEndpoint) {
		UUID tableID = UUID.fromString(message.getMessage());
		Table table = CommonEndpoint.TABLES.get(tableID);
		if (table == null) {
			LOGGER.info("No Such Table to watch:" + tableID);
			return null;
		}
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
		Message resultMessage = new Message();
		resultMessage.setFrom(SYSTEM);
		resultMessage.setTable(table);
		if (table.isGameOver()) {
			table.onGameOver();
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
		if (!table.isPlayer(endpoint.getUser()) || !table.isGameOver()) {
			throw new IllegalArgumentException("Rematch not possible, user is not a player or game is not over");
		}
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
