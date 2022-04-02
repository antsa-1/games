package com.tauhka.games.messaging.handlers;

import static com.tauhka.games.core.util.Constants.SYSTEM;

import java.util.Optional;
import java.util.stream.Stream;

import com.tauhka.games.core.tables.Table;
import com.tauhka.games.messaging.Message;
import com.tauhka.games.messaging.MessageTitle;
import com.tauhka.games.messaging.PoolMessage;
import com.tauhka.games.pool.PoolTable;
import com.tauhka.games.pool.PoolTable.TurnResult;
import com.tauhka.games.pool.PoolTurn;
import com.tauhka.games.web.websocket.CommonEndpoint;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Default;

/** @author antsa-1 from GitHub 26 Mar 2022 **/

@Default
@Dependent
public class PoolTableHandler {

	public Message update(CommonEndpoint endpoint, Message message) {
		Table table = findUserTable(endpoint);

		if (!table.isPlayerInTurn(endpoint.getUser())) {
			// throw new IllegalArgumentException("pool, not updating cue, player is not in turn");
			return null; // after turn ui not working properly, it still send updates..
		}
		Message updateMessage = new Message();
		updateMessage.setFrom(SYSTEM);
		updateMessage.setTable(table);
		updateMessage.setTitle(MessageTitle.POOL_UPDATE);
		PoolMessage updateCueMessage = new PoolMessage();
		updateCueMessage.setCue(message.getPoolMessage().getCue());
		updateCueMessage.setCueBall(message.getPoolMessage().getCueBall());
		updateCueMessage.setCanvas(message.getPoolMessage().getCanvas());
		updateMessage.setPoolMessage(updateCueMessage);
		return updateMessage;
	}

	public Message playTurn(CommonEndpoint endpoint, Message message) {
		// ServerSide calcs?
		PoolTurn turn = new PoolTurn();
		turn.setCanvas(message.getPoolMessage().getCanvas());
		turn.setCue(message.getPoolMessage().getCue());
		turn.setCueBall(message.getPoolMessage().getCueBall());
		PoolTable table = (PoolTable) findUserTable(endpoint);
		PoolTurn playedTurn = (PoolTurn) table.playTurn(endpoint.getUser(), turn);
		Message playTurnMessage = new Message();
		playTurnMessage.setFrom(SYSTEM);
		playTurnMessage.setTable(table);
		if (playedTurn.getTurnResult() == TurnResult.EIGHT_BALL_SUCCESS || playedTurn.getTurnResult() == TurnResult.EIGHT_BALL_SUCCESS) {
			playTurnMessage.setTitle(MessageTitle.GAME_END);
		} else if (playedTurn.getTurnResult() == TurnResult.HANDBALL) {
			playTurnMessage.setTitle(MessageTitle.POOL_SET_CUEBALL);
		} else {
			playTurnMessage.setTitle(MessageTitle.POOL_PLAY_TURN);
		}
		PoolMessage updateCueMessage = new PoolMessage();
		updateCueMessage.setCue(message.getPoolMessage().getCue());
		updateCueMessage.setCueBall(message.getPoolMessage().getCueBall());
		//playTurnMessage.setTable(table);
		updateCueMessage.setCanvas(message.getPoolMessage().getCanvas());
		playTurnMessage.setPoolMessage(updateCueMessage);
		return playTurnMessage;
	}

	public Table findUserTable(CommonEndpoint endpoint) {
		Stream<Table> stream = CommonEndpoint.TABLES.values().stream();
		stream = stream.filter(table -> table.isPlayer(endpoint.getUser()));
		Optional<Table> tableOptional = stream.findFirst();
		if (tableOptional.isEmpty()) {
			throw new IllegalArgumentException("no table for:" + endpoint.getUser());
		}
		return tableOptional.get();
	}
}
