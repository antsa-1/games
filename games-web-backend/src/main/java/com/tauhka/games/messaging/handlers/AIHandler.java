package com.tauhka.games.messaging.handlers;

import static com.tauhka.games.core.util.Constants.OLAV_COMPUTER;
import static com.tauhka.games.core.util.Constants.OLAV_COMPUTER_CONNECT_FOUR_RANKING;
import static com.tauhka.games.core.util.Constants.OLAV_COMPUTER_EIGHT_BALL_RANKING;
import static com.tauhka.games.core.util.Constants.OLAV_COMPUTER_ID;
import static com.tauhka.games.core.util.Constants.OLAV_COMPUTER_TICTACTOE_RANKING;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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
import com.tauhka.games.yatzy.Dice;
import com.tauhka.games.yatzy.Hand;
import com.tauhka.games.yatzy.HandType;
import com.tauhka.games.yatzy.YatzyAI;
import com.tauhka.games.yatzy.YatzyTable;
import com.tauhka.games.yatzy.util.HandSelector;
import com.tauhka.games.yatzy.util.HandWrapper;

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
		Hand hand = yatzyAI.deepContemplateHands(yatzyAI.getScoreCard(), table.getDices(), yatzyAI.getRollsLeft());
		if (hand != null) {
			message.setTitle(MessageTitle.YATZY_SELECT_HAND);
			yatzyMessage.setHandType(hand.getHandType().getAsInt());
			yatzyMessage.handType = hand.getHandType().getAsInt();
			return message;
		}
		// No suitable hand found, interesting dices have been selected
		message.setTitle(MessageTitle.YATZY_ROLL_DICES);
		yatzyMessage.dices = table.getDices().stream().filter(dice -> !dice.isSelected()).collect(Collectors.toList());
		return message;
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
