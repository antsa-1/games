package com.tauhka.games.messaging.handlers;

import static com.tauhka.games.core.util.Constants.SYSTEM;

import java.util.logging.Logger;

import com.tauhka.games.core.stats.GameStatisticsEvent;
import com.tauhka.games.core.stats.PoolGameStatisticsEvent;
import com.tauhka.games.core.tables.Table;
import com.tauhka.games.messaging.Message;
import com.tauhka.games.messaging.MessageTitle;
import com.tauhka.games.messaging.PoolMessage;
import com.tauhka.games.web.websocket.CommonEndpoint;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;

/**
 * @author antsa-1 from GitHub 12 May 2022
 **/

@Default
@Dependent
public class YatzyTableHandler extends CommonHandler {
	private static final Logger LOGGER = Logger.getLogger(YatzyTableHandler.class.getName());
	@Inject
	private Event<PoolGameStatisticsEvent> poolStatsEvent;
	@Inject
	private Event<GameStatisticsEvent> statisticsEvent;

	public Message handleYatzyMessage(CommonEndpoint endpoint, Message message) {
		Table table = findUserTable(endpoint); // this can become

		if (!table.isPlayerInTurn(endpoint.getUser())) {
			return null;
		}
		Message updateMessage = new Message();
		updateMessage.setFrom(SYSTEM);
		updateMessage.setTable(table);
		updateMessage.setTitle(MessageTitle.POOL_UPDATE);
		PoolMessage updateCueMessage = new PoolMessage();
		updateCueMessage.setCue(message.getPoolMessage().getCue());
		// updateCueMessage.setCueBall(message.getPoolMessage().getCueBall());
		// updateCueMessage.setCanvas(message.getPoolMessage().getCanvas());
		updateMessage.setPoolMessage(updateCueMessage);
		return updateMessage;
	}
}
