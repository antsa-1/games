package com.tauhka.common.messaging.handlers;

import static com.tauhka.games.core.util.Constants.SYSTEM;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import com.tauhka.common.messaging.Message;
import com.tauhka.common.messaging.MessageTitle;
import com.tauhka.common.web.CommonEndpoint;
import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.Move;
import com.tauhka.games.core.Table;
import com.tauhka.games.core.User;
import com.tauhka.games.core.twodimen.ArtificialUser;
import com.tauhka.games.core.twodimen.GameResult;

import jakarta.ejb.ConcurrentAccessException;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Default;

@Default
@Dependent
public class TableHandler {



	public Message createTable(Message message, CommonEndpoint endpoint) {
		Stream<Table> stream = CommonEndpoint.TABLES.values().stream();
		stream = stream.filter(table -> table.isPlayer(endpoint.getUser()));
		Optional<Table> tableOptional = stream.findFirst();
		if (tableOptional.isPresent()) {
			throw new IllegalArgumentException("User is already in table:" + endpoint.getUser() + " table:" + tableOptional.get());
		}
		GameMode gameMode = GameMode.getGameMode(Integer.parseInt(message.getMessage()));
		Table table = new Table(endpoint.getUser(), gameMode, false);
		CommonEndpoint.TABLES.put(table.getId(), table);
		if (message.getComputer()) {
			ArtificialUser user = new ArtificialUser();
			user.setName("Olav_computer");
			table.setPlayerB(user);
			Message message_ = new Message();
			message_.setTitle(MessageTitle.START_GAME);
			message_.setTable(table);
			return message_;
		} else {
			Message message_ = new Message();
			message_.setTitle(MessageTitle.CREATE_TABLE);
			message_.setTable(table);
			return message_;
		}
	}

	public Message leaveTable(Message mes, CommonEndpoint endpoint) {
		UUID tableId = UUID.fromString(mes.getMessage());
		Table table = CommonEndpoint.TABLES.get(tableId);
		if (table == null) {
			return null;
		}
		Message message = new Message();
		message.setTitle(MessageTitle.LEAVE_TABLE);
		message.setTable(table);
		User user = endpoint.getUser();
		if (table.removePlayerIfExist(user) || table.removeWatcherIfExist(user)) {
			message.setWho(endpoint.getUser());
			return message;
		}
		return null;
	}

	public Message createRemoveTableMessage(Table table, CommonEndpoint endpoint) {
		if (table == null) {
			return null;
		}
		if (table.getPlayerInTurn() == null) {
			Message message = new Message();
			message.setTitle(MessageTitle.REMOVE_TABLE);
			message.setTable(table);
			return message;
		}

		return null;

	}

	public Message addTokenToGame(Message message, User user) {
		Stream<Table> stream = CommonEndpoint.TABLES.values().stream();
		stream = stream.filter(table -> table.isPlayer(user));
		Optional<Table> tableOptional = stream.findFirst(); // Crash if user has earlier table, find wrong one.
		Message tokenMessage = new Message();
		if (tableOptional.isPresent()) {
			int x = message.getX();
			int y = message.getY();
			String tokenAdded = tableOptional.get().addGameToken(user, x, y);
			GameResult result = tableOptional.get().checkWinAndDraw();
			if (result != null) {
				tokenMessage.setTitle(MessageTitle.GAME_END);
			} else {
				tokenMessage.setTitle(MessageTitle.MOVE);
			}
			tokenMessage.setTable(tableOptional.get());
			tokenMessage.setMessage(tokenAdded);
			tokenMessage.setWin(result);
			tokenMessage.setX(x);
			tokenMessage.setY(y);
			return tokenMessage;
		}
		throw new IllegalArgumentException("No table to put token: for:" + user);
	}

	public Message removeEndpointOwnTable(CommonEndpoint endpoint) {
		// Using params would be faster...
		Optional<Table> table = CommonEndpoint.TABLES.values().stream().filter(taabel -> taabel.getPlayerA() != null).filter(taabel -> taabel.getPlayerA().equals(endpoint.getUser())).findFirst();
		if (table.isPresent()) {
			Table removedTable = CommonEndpoint.TABLES.remove(table.get().getId());
			Message message_ = new Message();
			message_.setTitle(MessageTitle.REMOVE_TABLE);
			message_.setTable(removedTable);
			return message_;
		}
		throw new IllegalArgumentException("No playerA found in tables:" + endpoint.getUser());
	}

	public synchronized Message joinTable(Message message, CommonEndpoint endpoint) {
		// Using params would be faster...
		UUID tableID = UUID.fromString(message.getMessage());
		Table table = CommonEndpoint.TABLES.get(tableID);
		if (!table.isWaitingOpponent()) {
			throw new ConcurrentAccessException("Table is not waiting player " + table + " trying user:" + endpoint.getUser());
		}
		if (table.getPlayerA().equals(endpoint.getUser())) {
			throw new IllegalArgumentException("Same players to table not possible..");
		}
		table.setPlayerB(endpoint.getUser());
		Message message_ = new Message();
		message_.setTitle(MessageTitle.START_GAME);
		message_.setTable(table);
		return message_;
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
		// Using params would be faster...
		Stream<Table> stream = CommonEndpoint.TABLES.values().stream();
		stream = stream.filter(table -> table.isPlayer(endpoint.getUser()));
		Optional<Table> tableOptional = stream.findFirst();
		if (tableOptional.isEmpty()) {
			throw new IllegalArgumentException("resign not possible, no table for:" + endpoint.getUser());

		}
		Table table = tableOptional.get();
		User winner = table.resign(endpoint.getUser());
		if (winner != null) {
			// For Artificial player set Rematch-state ready
			if (table.getOpponent(endpoint.getUser()) instanceof ArtificialUser) {
				table.suggestRematch(table.getOpponent(endpoint.getUser()));
			}
			Message winnerMessage = new Message();
			winnerMessage.setFrom(SYSTEM);
			// chatMessage.setTo(t);
			winnerMessage.setMessage("R"); // R=resignition in UI
			winnerMessage.setTable(table);
			winnerMessage.setWho(winner);
			winnerMessage.setTitle(MessageTitle.WINNER);
			return winnerMessage;
		}

		throw new IllegalArgumentException("resign not possible, is a player?. PlayerIn turn missing?" + endpoint.getUser());
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

	public Message makeComputerMove(Table table) {
		ArtificialUser artificialUser = (ArtificialUser) table.getPlayerInTurn();
		Move move = artificialUser.calculateBestMove(table);
		Message message = new Message();
		message.setTable(table);
		message.setTitle(MessageTitle.MOVE);
		message.setX(move.getX());
		message.setY(move.getY());
		return addTokenToGame(message, artificialUser);

	}
}
