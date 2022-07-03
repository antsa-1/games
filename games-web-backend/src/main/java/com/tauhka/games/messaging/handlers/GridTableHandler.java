package com.tauhka.games.messaging.handlers;

import java.util.UUID;

import com.tauhka.games.core.Move;
import com.tauhka.games.core.User;
import com.tauhka.games.core.tables.Table;
import com.tauhka.games.core.twodimen.GameResult;
import com.tauhka.games.messaging.Message;
import com.tauhka.games.messaging.MessageTitle;
import com.tauhka.games.messaging.util.GamesUtils;
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
		UUID tableId = GamesUtils.validateTableId(message);
		Table table = CommonEndpoint.TABLES.get(tableId);
		if (!table.isPlayer(user)) {
			throw new IllegalArgumentException("User:" + user + " is not a player in table:" + table);
		}
		Message tokenMessage = new Message();
		int x = message.getX();
		int y = message.getY();
		Move moveIn = new Move(x, y);
		Move move = (Move) table.playTurn(user, moveIn);
		GameResult result = table.checkWinAndDraw();
		if (result != null) {
			tokenMessage.setTitle(MessageTitle.GAME_END);
			table.setGameOver(true);
			fireStatisticsEventAsync(table, result);
		} else {
			tokenMessage.setTitle(MessageTitle.MOVE);
		}
		tokenMessage.setTable(table);
		tokenMessage.setMessage(move.toString());
		tokenMessage.setGameResult(result);
		tokenMessage.setX(move.getX());
		tokenMessage.setY(move.getY());
		tokenMessage.setToken(move.getToken().getAsText());
		return tokenMessage;
	}

}
