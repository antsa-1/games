package com.tauhka.games.messaging.handlers;

import static com.tauhka.games.core.util.Constants.SYSTEM;

import java.util.List;
import java.util.logging.Logger;

import com.tauhka.games.core.stats.MultiplayerRankingCalculator;
import com.tauhka.games.db.YatzyStatsEJB;
import com.tauhka.games.db.dao.YatzyTurnDao;
import com.tauhka.games.messaging.Message;
import com.tauhka.games.messaging.MessageTitle;
import com.tauhka.games.messaging.YatzyMessage;
import com.tauhka.games.web.websocket.CommonEndpoint;
import com.tauhka.games.yatzy.Dice;
import com.tauhka.games.yatzy.ScoreCard;
import com.tauhka.games.yatzy.YatzyTable;

import jakarta.enterprise.context.Dependent;
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
	YatzyStatsEJB yatzyStatsEJB;

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
			createTurnDAOForRolledDices(table, rolledDices);
		} else if (incomingMessage.getTitle() == MessageTitle.YATZY_SELECT_HAND) {
			ScoreCard sc = table.selectHand(endpoint.getUser(), incomingMessage.getYatzyMessage().handVal);
			saveHandSelectionToDatabase(table, sc);
			playedTurnMessage = new YatzyMessage();
			boolean gameOver = table.isGameOver();
			if (!gameOver) {
				table.changePlayerInTurn();
			} else {
				table.checkWinAndDraw();
				playedTurnMessage.setGameOver(gameOver);
				//MultiplayerRankingCalculator.calculateNewRankings(table.getGameResult());
			}
			playedTurnMessage.setScoreCard(sc);
			playedTurnMessage.setWhoPlayed(endpoint.getUser().getName());
			updateMessage.setTitle(MessageTitle.YATZY_SELECT_HAND);
		}
		updateMessage.setFrom(SYSTEM);
		updateMessage.setTable(table);
		updateMessage.setYatzyMessage(playedTurnMessage);
		return updateMessage;
	}

	private void createTurnDAOForRolledDices(YatzyTable table, List<Dice> rolledDices) {

		// Set diceValue in current position with a new value if it has been rolled, otherwise null
		Integer diceValue1 = rolledDices.stream().filter(dice -> table.getDices().get(0).getDiceId().equals(dice.getDiceId())).findFirst().isPresent() ? table.getDices().get(0).getNumber() : null;
		Integer diceValue2 = rolledDices.stream().filter(dice -> table.getDices().get(1).getDiceId().equals(dice.getDiceId())).findFirst().isPresent() ? table.getDices().get(1).getNumber() : null;
		Integer diceValue3 = rolledDices.stream().filter(dice -> table.getDices().get(2).getDiceId().equals(dice.getDiceId())).findFirst().isPresent() ? table.getDices().get(2).getNumber() : null;
		Integer diceValue4 = rolledDices.stream().filter(dice -> table.getDices().get(3).getDiceId().equals(dice.getDiceId())).findFirst().isPresent() ? table.getDices().get(3).getNumber() : null;
		Integer diceValue5 = rolledDices.stream().filter(dice -> table.getDices().get(4).getDiceId().equals(dice.getDiceId())).findFirst().isPresent() ? table.getDices().get(4).getNumber() : null;
		YatzyTurnDao dao = new YatzyTurnDao(table.getPlayerInTurn().getName(), table.getPlayerInTurn().getId(), diceValue1, diceValue2, diceValue3, diceValue4, diceValue5, null, null, table.getGameId());
		yatzyStatsEJB.saveYatzyTurn(dao);
	}

	private void saveHandSelectionToDatabase(YatzyTable table, ScoreCard sc) {
		int score = sc.getLastAdded().getValue();
		String handType = sc.getLastAdded().getHandType().toString();
		Integer diceValue1 = table.getDices().get(0).getNumber();
		Integer diceValue2 = table.getDices().get(1).getNumber();
		Integer diceValue3 = table.getDices().get(2).getNumber();
		Integer diceValue4 = table.getDices().get(3).getNumber();
		Integer diceValue5 = table.getDices().get(4).getNumber();
		YatzyTurnDao dao = new YatzyTurnDao(table.getPlayerInTurn().getName(), table.getPlayerInTurn().getId(), diceValue1, diceValue2, diceValue3, diceValue4, diceValue5, score, handType, table.getGameId());
		yatzyStatsEJB.saveYatzyTurn(dao);
	}
}
