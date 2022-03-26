package com.tauhka.games.messaging.handlers;

import static com.tauhka.games.core.util.Constants.SYSTEM;

import java.util.Optional;
import java.util.stream.Stream;

import com.tauhka.games.core.tables.Table;
import com.tauhka.games.messaging.Message;
import com.tauhka.games.messaging.MessageTitle;
import com.tauhka.games.messaging.PoolMessage;
import com.tauhka.games.web.websocket.CommonEndpoint;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Default;

/** @author antsa-1 from GitHub 26 Mar 2022 **/
@Default
@Dependent
public class PoolTableHandler {

	public Message updateCue(CommonEndpoint endpoint, Message message) {
		Stream<Table> stream = CommonEndpoint.TABLES.values().stream();
		stream = stream.filter(table -> table.isPlayer(endpoint.getUser()));
		Optional<Table> tableOptional = stream.findFirst();
		if (tableOptional.isEmpty()) {
			throw new IllegalArgumentException("no table to update cue" + endpoint.getUser());
		}
		Table table = tableOptional.get();
		/* not recycling the given message directly, although creating new objects increases memory/heap size. Reason is that message could contain some unwanted data in the other fields and passing it directly to other user without
		 * sanitizing, and that is not wanted effect */

		Message updateMessage = new Message();
		updateMessage.setFrom(SYSTEM);
		updateMessage.setTable(table);
		updateMessage.setTitle(MessageTitle.POOL_CUE_UPDATE);
		PoolMessage updateCueMessage = new PoolMessage();
		updateCueMessage.setCueAngle(message.getPoolMessage().getCueAngle());
		updateCueMessage.setCueBallPosition(message.getPoolMessage().getCueBallPosition());
		updateCueMessage.setCanvas(message.getPoolMessage().getCanvas());
		updateMessage.setPoolMessage(updateCueMessage);
		return updateMessage;
	}

	public Message playTurn(CommonEndpoint endpoint, PoolMessage message) {
		throw new RuntimeException("todo");

	}
}
