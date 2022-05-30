package com.tauhka.games.messaging.handlers;

import java.util.Optional;
import java.util.stream.Stream;

import com.tauhka.games.core.Move;
import com.tauhka.games.core.User;
import com.tauhka.games.core.tables.Table;
import com.tauhka.games.core.twodimen.GameResult;
import com.tauhka.games.messaging.Message;
import com.tauhka.games.messaging.MessageTitle;
import com.tauhka.games.web.websocket.CommonEndpoint;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Default;

/**
 * @author antsa-1 from GitHub 30 May 2022
 **/
@Default
@Dependent
public class GridTableHandler extends CommonHandler {
	public Message handleNewToken(Message message, User user) {
		Stream<Table> stream = CommonEndpoint.TABLES.values().stream();
		stream = stream.filter(table -> table.isPlayer(user));

		Optional<Table> tableOptional = stream.findFirst(); // Crash if user has earlier table, find wrong one.
		Message tokenMessage = new Message();
		if (tableOptional.isPresent()) {
			int x = message.getX();
			int y = message.getY();
			Table table = tableOptional.get();
			Move moveIn = new Move(x, y);
			Move move = (Move) table.playTurn(user, moveIn);
			GameResult result = tableOptional.get().checkWinAndDraw();
			if (result != null) {
				tokenMessage.setTitle(MessageTitle.GAME_END);
				table.setGameOver(true);
				fireStatisticsEventAsync(table, result);
			} else {
				tokenMessage.setTitle(MessageTitle.MOVE);
			}
			tokenMessage.setTable(tableOptional.get());
			tokenMessage.setMessage(move.toString());
			tokenMessage.setGameResult(result);
			tokenMessage.setX(move.getX());
			tokenMessage.setY(move.getY());
			tokenMessage.setToken(move.getToken().getAsText());
			return tokenMessage;
		}
		throw new IllegalArgumentException("No table to put token: for:" + user);
	}

}
