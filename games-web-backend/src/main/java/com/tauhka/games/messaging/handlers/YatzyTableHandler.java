package com.tauhka.games.messaging.handlers;

import static com.tauhka.games.core.util.Constants.SYSTEM;

import java.util.List;
import java.util.logging.Logger;

import com.tauhka.games.core.GameResultType;
import com.tauhka.games.core.stats.GameStatisticsEvent;
import com.tauhka.games.messaging.Message;
import com.tauhka.games.messaging.MessageTitle;
import com.tauhka.games.messaging.YatzyMessage;
import com.tauhka.games.web.websocket.CommonEndpoint;
import com.tauhka.games.yatzy.Dice;
import com.tauhka.games.yatzy.ScoreCard;
import com.tauhka.games.yatzy.YatzyTable;

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
	private Event<GameStatisticsEvent> statisticsEvent;

	public Message handleYatzyMessage(CommonEndpoint endpoint, Message incomingMessage) {
		YatzyTable table = (YatzyTable) findUserTable(endpoint); // this can become

		if (!table.isPlayerInTurn(endpoint.getUser())) {
			throw new IllegalArgumentException("Player is not in turn" + endpoint.getUser());
		}
		if (incomingMessage.getYatzyMessage() == null) {
			throw new IllegalArgumentException("No yatzyMessage");
		}
		YatzyMessage playedTurnMessage = null;
		Message updateMessage = new Message();
		if (incomingMessage.getTitle() == MessageTitle.YATZY_ROLL_DICES) {
			List<Dice> rolledDices = table.rollDices(endpoint.getUser(), incomingMessage.getYatzyMessage().dices);
			playedTurnMessage = new YatzyMessage();
			playedTurnMessage.setDices(rolledDices);
			playedTurnMessage.setWhoPlayed(endpoint.getUser().getName());
			updateMessage.setTitle(MessageTitle.YATZY_ROLL_DICES);
		} else if (incomingMessage.getTitle() == MessageTitle.YATZY_SELECT_HAND) {
			ScoreCard sc = table.selectHand(endpoint.getUser(), incomingMessage.getYatzyMessage().handVal);
			playedTurnMessage = new YatzyMessage();
			boolean gameOver = table.isGameOver();
			if (!gameOver) {
				table.changePlayerInTurn();
			} else {
				playedTurnMessage.setGameOver(table.isGameOver());
			}
			//playedTurnMessage.setGameOver(table.isGameOver());
			playedTurnMessage.setScoreCard(sc);
			playedTurnMessage.setWhoPlayed(endpoint.getUser().getName());
			updateMessage.setTitle(MessageTitle.YATZY_SELECT_HAND);
		}
		updateMessage.setFrom(SYSTEM);
		updateMessage.setTable(table);
		updateMessage.setYatzyMessage(playedTurnMessage);
		return updateMessage;
	}

	private void handleGameEndStats(YatzyTable table) {
		// TODO Auto-generated method stub

	}
}
