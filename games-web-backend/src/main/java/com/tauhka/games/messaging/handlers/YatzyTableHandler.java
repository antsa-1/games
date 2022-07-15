package com.tauhka.games.messaging.handlers;

import static com.tauhka.games.core.util.Constants.SYSTEM;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tauhka.games.core.User;
import com.tauhka.games.core.events.AITurnEvent;
import com.tauhka.games.core.tables.Table;
import com.tauhka.games.db.YatzyEventEJB;
import com.tauhka.games.db.dao.YatzyTurnDao;
import com.tauhka.games.messaging.Message;
import com.tauhka.games.messaging.MessageTitle;
import com.tauhka.games.messaging.YatzyMessage;
import com.tauhka.games.web.websocket.CommonEndpoint;
import com.tauhka.games.yatzy.Dice;
import com.tauhka.games.yatzy.ScoreCard;
import com.tauhka.games.yatzy.YatzyTable;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;

/**
 * t
 * 
 * @author antsa-1 from GitHub 12 May 2022
 **/

@Default
@Dependent
public class YatzyTableHandler extends CommonHandler {
	private static final Logger LOGGER = Logger.getLogger(YatzyTableHandler.class.getName());
	@Inject
	private YatzyEventEJB yatzyStatsEJB;
	@Inject
	private AIHandler aiHandler;

	public void handleYatzyMessage(CommonEndpoint endpoint, Message incomingMessage) {
		YatzyTable table = (YatzyTable) findPlayerTable(endpoint.getUser(), incomingMessage);

		if (!table.isPlayerInTurn(endpoint.getUser())) {
			throw new IllegalArgumentException("Player is not in turn" + endpoint.getUser());
		}
		if (incomingMessage.getYatzyMessage() == null) {
			throw new IllegalArgumentException("No yatzyMessage");
		}
		YatzyMessage yatzyMessage = null;
		Message updateMessage = new Message();
		if (incomingMessage.getTitle() == MessageTitle.YATZY_ROLL_DICES) {
			yatzyMessage = rollDices(table.getPlayerInTurn(), incomingMessage, table);
			updateMessage.setTitle(MessageTitle.YATZY_ROLL_DICES);
		} else if (incomingMessage.getTitle() == MessageTitle.YATZY_SELECT_HAND) {
			updateMessage.setTitle(MessageTitle.YATZY_SELECT_HAND);
			yatzyMessage = selectHand(table.getPlayerInTurn(), incomingMessage, table);
		}
		updateMessage.setFrom(SYSTEM);
		updateMessage.setTable(table);
		updateMessage.setYatzyMessage(yatzyMessage);
		sendMessageToTable(table, updateMessage);
		if (table.isArtificialPlayerInTurn()) {
			playComputerTurn(table);
		}
		if (table.isGameOver()) {
			table.onGameOver();
		}
	}

	private YatzyMessage selectHand(User user, Message incomingMessage, YatzyTable table) {
		ScoreCard sc = table.selectHand(user, incomingMessage.getYatzyMessage().handType);
		saveHandSelectionToDatabase(table, sc);
		YatzyMessage playedTurnMessage = new YatzyMessage();
		boolean gameOver = table.isGameOver();
		if (!gameOver) {
			table.changePlayerInTurn();
		} else {
			table.checkWinAndDraw();
			playedTurnMessage.setGameOver(gameOver);
			// MultiplayerRankingCalculator.calculateNewRankings(table.getGameResult());
		}
		playedTurnMessage.setScoreCard(sc);
		playedTurnMessage.setWhoPlayed(user.getName());
		return playedTurnMessage;
	}

	private YatzyMessage rollDices(User user, Message incomingMessage, YatzyTable table) {
		YatzyMessage playedTurnMessage;
		List<Dice> rolledDices = table.rollDices(user, incomingMessage.getYatzyMessage().dices);
		playedTurnMessage = new YatzyMessage();
		playedTurnMessage.setDices(rolledDices);
		playedTurnMessage.setWhoPlayed(user.getName());
		Message updateMessage = new Message();
		updateMessage.setTitle(MessageTitle.YATZY_ROLL_DICES);
		createTurnDAOForRolledDices(table, rolledDices);
		return playedTurnMessage;
	}

	private int getWaitTime(Table table) {
		return table.getTimeControlIndex() > 1 ? 4000 : 50;
	}

	public void playComputerTurn(Table table) {
		int finalExitCondition = 0; // 4 moves maximum
		YatzyTable yatzyTable = (YatzyTable) table;
		while (table.isArtificialPlayerInTurn() && finalExitCondition <= 4) {
			try {
				finalExitCondition++;
				Thread.sleep(getWaitTime(yatzyTable));
				Message nextMoveMessage = aiHandler.calculateNextYatzyMove(yatzyTable);
				YatzyMessage yMessage = null;
				if (nextMoveMessage.getTitle() == MessageTitle.YATZY_SELECT_HAND) {
					yMessage = selectHand(table.getPlayerInTurn(), nextMoveMessage, yatzyTable);
				} else {
					yMessage = rollDices(table.getPlayerInTurn(), nextMoveMessage, yatzyTable);
				}
				nextMoveMessage.setTable(table);
				nextMoveMessage.setYatzyMessage(yMessage);
				sendMessageToTable(table, nextMoveMessage);
			} catch (InterruptedException e) {
				LOGGER.severe("YatzyTableHandler exception in playing computer turns");
			}
			if (finalExitCondition == 4) {
				break;
			}
		}
	}

	@SuppressWarnings("unused")
	private void observeTablesWhereAIActionIsRequired(@ObservesAsync AITurnEvent turnEvent) {
		LOGGER.fine("YatzyTableHandler received event, computer is expected to play now in table" + turnEvent.getTable());
		if (turnEvent.getDelay() > 0) {
			try {
				Thread.sleep(turnEvent.getDelay());
				playComputerTurn(turnEvent.getTable());
			} catch (InterruptedException e) {
				LOGGER.log(Level.SEVERE, "YatzyTableHandler: wait before computer turn was interruped.", e);
			}
		} else {
			playComputerTurn(turnEvent.getTable());
		}
	}

	private void createTurnDAOForRolledDices(YatzyTable table, List<Dice> rolledDices) {

		// Set diceValue in current position with a new value if it has been rolled,
		// otherwise null
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
