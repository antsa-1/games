package com.tauhka.games.messaging.handlers;

import static com.tauhka.games.core.util.Constants.OLAV_COMPUTER;
import static com.tauhka.games.core.util.Constants.OLAV_COMPUTER_CONNECT_FOUR_RANKING;
import static com.tauhka.games.core.util.Constants.OLAV_COMPUTER_EIGHT_BALL_RANKING;
import static com.tauhka.games.core.util.Constants.OLAV_COMPUTER_ID;
import static com.tauhka.games.core.util.Constants.OLAV_COMPUTER_TICTACTOE_RANKING;

import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.Move;
import com.tauhka.games.core.User;
import com.tauhka.games.core.ai.ArtificialUser;
import com.tauhka.games.core.tables.Table;
import com.tauhka.games.core.tables.TicTacToeTable;
import com.tauhka.games.messaging.Message;
import com.tauhka.games.messaging.MessageTitle;
import com.tauhka.games.messaging.YatzyMessage;
import com.tauhka.games.pool.PoolAI;
import com.tauhka.games.yatzy.Hand;
import com.tauhka.games.yatzy.YatzyAI;
import com.tauhka.games.yatzy.YatzyTable;
import com.tauhka.games.yatzy.util.HandSelector;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;

/**
 * @author antsa-1 from GitHub 30 May 2022
 **/
@Default
@Dependent
public class AIHandler {
	@Inject
	private GridTableHandler gridTableHandler;
	private static final Logger LOGGER = Logger.getLogger(AIHandler.class.getName());

	public User createAIPlayer(GameMode gameMode) {
		User user = gameMode.getGameNumber() == GameMode.POOL ? new PoolAI() : new ArtificialUser();
		user.setName(OLAV_COMPUTER);
		user.setRankingTictactoe(OLAV_COMPUTER_TICTACTOE_RANKING);
		user.setRankingConnectFour(OLAV_COMPUTER_CONNECT_FOUR_RANKING);
		user.setRankingEightBall(OLAV_COMPUTER_EIGHT_BALL_RANKING);
		user.setId(UUID.fromString(OLAV_COMPUTER_ID));
		return user;
	}

	public Message makeGridTableMove(Table table) {
		return createGridTableMove(table);
	}

	public Message calculateNextYatzyMove(YatzyTable table) {
		if (!(table.isArtificialPlayerInTurn())) {
			LOGGER.info("Olav_Computer has timed out while thread was sleeping. TimeControlIndex:" + table.getTimeControlIndex());
			throw new IllegalStateException("Not computer turn");
		}
		YatzyAI yatzyAI = (YatzyAI) table.getPlayerInTurn();
		YatzyTable yatzyTable = (YatzyTable) table;
		YatzyMessage yatzyMessage = new YatzyMessage();
		Message message = new Message();
		message.setYatzyMessage(yatzyMessage);
		if (yatzyAI.getRollsLeft() == 3) {
			message.setTitle(MessageTitle.YATZY_ROLL_DICES);
			yatzyMessage.dices = yatzyTable.getDices();
			return message;
		}
		Hand hand = yatzyAI.deepContemplateHands(yatzyAI.getScoreCard(), table.getDices());
		yatzyMessage.dices = table.getDices().stream().filter(dice -> !dice.isSelected()).collect(Collectors.toList());
		hand = checkFallBack(table, yatzyAI, hand, message);
		if (hand != null && hand.getHandType() != null) {
			message.setTitle(MessageTitle.YATZY_SELECT_HAND);
			yatzyMessage.setHandType(hand.getHandType().getAsInt());
			return message;
		}
		message.setTitle(MessageTitle.YATZY_ROLL_DICES);
		checkFallBack(table, yatzyAI, hand, message);
		return message;
	}

	private Hand checkFallBack(YatzyTable table, YatzyAI yatzyAI, Hand hand, Message message) {
		// Fallback is used for not creating enough unit tests
		if (MessageTitle.YATZY_SELECT_HAND == message.getTitle() && hand == null) {
			LOGGER.info("Yatzy using handSelecion fallBack \n" + table);
			hand = HandSelector.getMostValuableHands(yatzyAI.getScoreCard(), table.getDices()).getMostValuable();
			return hand;
		}
		if (!yatzyAI.hasRollsLeft() && message.getTitle() == MessageTitle.YATZY_ROLL_DICES) {
			LOGGER.info("Yatzy using handSelecion fallBack 2 \n" + table);
			hand = HandSelector.getMostValuableHands(yatzyAI.getScoreCard(), table.getDices()).getMostValuable();
			message.setTitle(MessageTitle.YATZY_SELECT_HAND);
			message.getYatzyMessage().setHandType(hand.getHandType().getAsInt());
		}
		if ((message.getYatzyMessage().getDices().size() == 0 && message.getTitle() == MessageTitle.YATZY_ROLL_DICES)) {
			LOGGER.info("Yatzy using handSelecion fallBack 3 \n" + table);
			hand = HandSelector.getMostValuableHands(yatzyAI.getScoreCard(), table.getDices()).getMostValuable();
			message.setTitle(MessageTitle.YATZY_SELECT_HAND);
			message.getYatzyMessage().setHandType(hand.getHandType().getAsInt());
		}
		return hand;
	}

	private Message createGridTableMove(Table table) {
		ArtificialUser artificialUser = (ArtificialUser) table.getPlayerInTurn();
		Move move = artificialUser.calculateBestMove((TicTacToeTable) table);
		Message message = new Message();
		message.setTable(table);
		message.setTitle(MessageTitle.MOVE);
		message.setX(move.getX());
		message.setY(move.getY());
		message.setMessage(table.getTableId().toString());
		return gridTableHandler.handleNewToken(message, artificialUser);
	}
}
