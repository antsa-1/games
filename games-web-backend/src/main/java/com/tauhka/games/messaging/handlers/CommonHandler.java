package com.tauhka.games.messaging.handlers;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.tauhka.games.core.GameResultType;
import com.tauhka.games.core.User;
import com.tauhka.games.core.stats.GameStatisticsEvent;
import com.tauhka.games.core.tables.Table;
import com.tauhka.games.core.tables.TableType;
import com.tauhka.games.core.twodimen.GameResult;
import com.tauhka.games.messaging.Message;
import com.tauhka.games.messaging.util.GamesUtils;
import com.tauhka.games.web.websocket.CommonEndpoint;

import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.websocket.EncodeException;

/**
 * @author antsa-1 from GitHub 12 May 2022
 **/

public class CommonHandler {
	@Inject
	private Event<GameStatisticsEvent> statisticsEvent;
	private static final Logger LOGGER = Logger.getLogger(CommonHandler.class.getName());
	
//	protected Table findUserTable(User user, Message message) {
//		UUID id = GamesUtils.validateTableId(message);
//		Table table = CommonEndpoint.TABLES.get(id);
//		// return user.getTable()
//		if (table == null || !table.isInTable(user)) {
//			throw new IllegalArgumentException("User was not found in table:" + user + " table:" + table);
//		}
//		return table;
//	}

	protected Table findPlayerTable(User user, Message message) {
		// return user.getTable()
		Stream<Table> stream = CommonEndpoint.TABLES.values().stream();
		stream = stream.filter(table -> table.isPlayer(user));
		Optional<Table> tableOptional = stream.findFirst();
		if (tableOptional.isPresent()) {
			return tableOptional.get();
		}
		throw new IllegalArgumentException("User is not player in table:" + user);
	}

	protected void fireStatisticsEventAsync(Table table, GameResult result) {
		GameStatisticsEvent statsEvent = new GameStatisticsEvent();
		statsEvent.setGameResult(result);
		statisticsEvent.fireAsync(statsEvent);
	}

	protected void fireStatisticsEventSync(Table table, GameResult result) {
		try {
			GameStatisticsEvent statsEvent = new GameStatisticsEvent();
			statsEvent.setGameResult(result);
			statisticsEvent.fire(statsEvent);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Leaving player sync stats failed:", e);
		}
	}

	protected void handleLeavingPlayerStatistics(Table table, CommonEndpoint endpoint) {
		if (table.isGameOver()) {
			// Statistics go another route since game ended "normally"
			return;
		}
		if (table.getTableType() == TableType.BASE) {
			table.setGameOver(true); // stats only once per game/leaver
		}
		User user = table.getOpponent(endpoint.getUser());
		GameResult result = new GameResult();
		result.setPlayerA(table.getPlayerA());
		result.setPlayerB(table.getPlayerB());
		result.setResultType(GameResultType.LEFT_ONGOING_GAME);
		result.setWinner(user);
		result.setGameMode(table.getGameMode());
		result.setStartInstant(table.getStartTime());
		fireStatisticsEventSync(table, result);
	}

	public void sendMessageToTable(Table table, Message message) {
		if (message == null) {
			return;
		}
		for (User user : table.getUsers()) {
			if (user.getWebsocketSession() != null && user.getWebsocketSession().isOpen()) {
				try {
					user.getWebsocketSession().getBasicRemote().sendObject(message);
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, "CommonHandler sendMessage to table io.error", e);
				} catch (EncodeException e) {
					LOGGER.log(Level.SEVERE, "CommonHandler sendMessage to table encoding error", e);
				}
			}
		}
	}
}
