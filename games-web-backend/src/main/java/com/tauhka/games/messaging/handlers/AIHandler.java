package com.tauhka.games.messaging.handlers;

import static com.tauhka.games.core.util.Constants.OLAV_COMPUTER;
import static com.tauhka.games.core.util.Constants.OLAV_COMPUTER_CONNECT_FOUR_RANKING;
import static com.tauhka.games.core.util.Constants.OLAV_COMPUTER_EIGHT_BALL_RANKING;
import static com.tauhka.games.core.util.Constants.OLAV_COMPUTER_ID;
import static com.tauhka.games.core.util.Constants.OLAV_COMPUTER_TICTACTOE_RANKING;

import java.util.UUID;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.Move;
import com.tauhka.games.core.User;
import com.tauhka.games.core.ai.ArtificialUser;
import com.tauhka.games.core.tables.Table;
import com.tauhka.games.core.tables.TicTacToeTable;
import com.tauhka.games.messaging.Message;
import com.tauhka.games.messaging.MessageTitle;
import com.tauhka.games.pool.PoolAI;

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

	public User createAIPlayer(GameMode gameMode) {
		User user = gameMode.getGameNumber() == GameMode.POOL ? new PoolAI() : new ArtificialUser();
		user.setName(OLAV_COMPUTER);
		user.setRankingTictactoe(OLAV_COMPUTER_TICTACTOE_RANKING);
		user.setRankingConnectFour(OLAV_COMPUTER_CONNECT_FOUR_RANKING);
		user.setRankingEightBall(OLAV_COMPUTER_EIGHT_BALL_RANKING);
		user.setId(UUID.fromString(OLAV_COMPUTER_ID));
		return user;
	}

	public Message makeComputerMove(Table table) {
		ArtificialUser artificialUser = (ArtificialUser) table.getPlayerInTurn();
		Move move = artificialUser.calculateBestMove((TicTacToeTable) table);
		Message message = new Message();
		message.setTable(table);
		message.setTitle(MessageTitle.MOVE);
		message.setX(move.getX());
		message.setY(move.getY());
		return gridTableHandler.handleNewToken(message, artificialUser);

	}
}
