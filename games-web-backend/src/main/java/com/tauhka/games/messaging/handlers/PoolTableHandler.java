package com.tauhka.games.messaging.handlers;

import static com.tauhka.games.core.util.Constants.SYSTEM;

import java.util.Optional;
import java.util.stream.Stream;

import com.tauhka.games.core.tables.Table;
import com.tauhka.games.messaging.Message;
import com.tauhka.games.messaging.MessageTitle;
import com.tauhka.games.messaging.PoolMessage;
import com.tauhka.games.pool.PoolTable;
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
		/* not recycling the given message directly, although creating new objects increases used memory/heap size. Reason is that message could contain some unwanted data in the other fields and passing it directly to other user without
		 * sanitizing, and that is not wanted effect */
		if (!table.isPlayerInTurn(endpoint.getUser())) {
			throw new IllegalArgumentException("pool, not updating cue, player is not in turn");
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
		Object notUsedAtm = table.playTurn(endpoint.getUser(), turn);
		Message updateMessage = new Message();
		updateMessage.setFrom(SYSTEM);
		updateMessage.setTable(table);
		updateMessage.setTitle(MessageTitle.POOL_PLAY_TURN);
		PoolMessage updateCueMessage = new PoolMessage();
		updateCueMessage.setCue(message.getPoolMessage().getCue());
		updateCueMessage.setCueBall(message.getPoolMessage().getCueBall());

		updateCueMessage.setCanvas(message.getPoolMessage().getCanvas());
		updateMessage.setPoolMessage(updateCueMessage);
		return updateMessage;
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
