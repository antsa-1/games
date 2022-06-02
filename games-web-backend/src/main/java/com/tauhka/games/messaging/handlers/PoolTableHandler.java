package com.tauhka.games.messaging.handlers;

import static com.tauhka.games.core.util.Constants.SYSTEM;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.tauhka.games.core.GameResultType;
import com.tauhka.games.core.User;
import com.tauhka.games.core.stats.GameStatisticsEvent;
import com.tauhka.games.core.stats.PoolGameStatisticsEvent;
import com.tauhka.games.core.tables.Table;
import com.tauhka.games.core.twodimen.GameResult;
import com.tauhka.games.core.twodimen.PoolTurnStats;
import com.tauhka.games.messaging.Message;
import com.tauhka.games.messaging.MessageTitle;
import com.tauhka.games.messaging.PoolMessage;
import com.tauhka.games.pool.PoolAI;
import com.tauhka.games.pool.PoolTable;
import com.tauhka.games.pool.PoolTurn;
import com.tauhka.games.pool.TurnResult;
import com.tauhka.games.pool.TurnType;
import com.tauhka.games.web.websocket.CommonEndpoint;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;

/** @author antsa-1 from GitHub 26 Mar 2022 **/

@Default
@Dependent
public class PoolTableHandler extends CommonHandler {
	private static final Logger LOGGER = Logger.getLogger(PoolTableHandler.class.getName());
	@Inject
	private Event<PoolGameStatisticsEvent> poolStatsEvent;
	@Inject
	private Event<GameStatisticsEvent> statisticsEvent;

	public Message updateCuePosition(CommonEndpoint endpoint, Message message) {
		Table table = findUserTable(endpoint); // this can become

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
		//updateCueMessage.setCueBall(message.getPoolMessage().getCueBall());
		//updateCueMessage.setCanvas(message.getPoolMessage().getCanvas());
		updateMessage.setPoolMessage(updateCueMessage);
		return updateMessage;
	}

	public Message selectPocket(CommonEndpoint endpoint, Message message) {
		PoolTable table = (PoolTable) findUserTable(endpoint);
		PoolTurn incomingTurn = new PoolTurn();
		incomingTurn.setCanvas(message.getPoolMessage().getCanvas());
		incomingTurn.setCue(message.getPoolMessage().getCue());
		incomingTurn.setCueBall(message.getPoolMessage().getCueBall());
		PoolTurn selectedPocketTurn = table.selectPocket(endpoint.getUser(), message.getPoolMessage().getSelectedPocket());
		Message updateMessage = new Message();
		updateMessage.setFrom(SYSTEM);
		updateMessage.setTable(table);
		updateMessage.setTitle(MessageTitle.POOL_SELECT_POCKET);
		PoolMessage selectPocketMessage = new PoolMessage();
		selectPocketMessage.setSelectedPocket(selectedPocketTurn.getSelectedPocket());
		selectPocketMessage.setCueBall(selectedPocketTurn.getCueBall());
		selectPocketMessage.setWhoPlayed(selectedPocketTurn.getPlayer());
		// selectPocketMessage.setCueBall(message.getPoolMessage().getCueBall());
		// selectPocketMessage.setCanvas(message.getPoolMessage().getCanvas());
		updateMessage.setPoolMessage(selectPocketMessage);
		updateMessage.setTable(table);
		addTurnToDatabase(incomingTurn, table, selectedPocketTurn, endpoint.getUser(), TurnType.POCKET_SELECTION);
		return updateMessage;
	}

	public Message updateHandBall(CommonEndpoint endpoint, Message incomingMessage) {
		PoolTable table = (PoolTable) findUserTable(endpoint);
		PoolTurn incomingTurn = new PoolTurn();
		incomingTurn.setCanvas(incomingMessage.getPoolMessage().getCanvas());
		incomingTurn.setCue(incomingMessage.getPoolMessage().getCue());
		incomingTurn.setCueBall(incomingMessage.getPoolMessage().getCueBall());
		PoolTurn turn = table.updateHandBall(endpoint.getUser(), incomingMessage.getPoolMessage().getCueBall());
		// Turn.java directly to PoolMessage, now only mapping.. TODO?
		Message updateMessage = new Message();
		updateMessage.setFrom(SYSTEM);
		updateMessage.setTable(table);
		updateMessage.setTitle(MessageTitle.POOL_HANDBALL);
		PoolMessage updateCueBallPositionMessage = new PoolMessage();
		updateCueBallPositionMessage.setCueBall(turn.getCueBall());
		updateCueBallPositionMessage.setCue(incomingMessage.getPoolMessage().getCue());
		updateCueBallPositionMessage.setWhoPlayed(turn.getPlayer());
		// updateCueBallPositionMessage.setCanvas(new Vector2d());
		updateCueBallPositionMessage.setTurnResult(turn.getTurnResult().toString());
		updateMessage.setPoolMessage(updateCueBallPositionMessage);
		updateMessage.setTable(table);
		addTurnToDatabase(incomingTurn, table, turn, endpoint.getUser(), TurnType.HANDBALL);
		return updateMessage;
	}

	public Message playTurn(CommonEndpoint endpoint, Message message) {
		PoolTurn incomingTurn = new PoolTurn();
		incomingTurn.setCanvas(message.getPoolMessage().getCanvas());
		incomingTurn.setCue(message.getPoolMessage().getCue());
		incomingTurn.setCueBall(message.getPoolMessage().getCueBall());
		PoolTable table = (PoolTable) findUserTable(endpoint);
		PoolTurn playedTurn = (PoolTurn) table.playTurn(endpoint.getUser(), incomingTurn);
		Message playTurnMessage = createPlayedTurnMessage(table, playedTurn);
		addTurnToDatabase(incomingTurn, table, playedTurn, endpoint.getUser(), TurnType.PLAY);
		return playTurnMessage;
	}

	private Message createPlayedTurnMessage(PoolTable table, PoolTurn playedTurn) {
		Message playTurnMessage = new Message();
		playTurnMessage.setFrom(SYSTEM);
		playTurnMessage.setTable(table);
		if (TurnResult.isDecisive(playedTurn.getTurnResult())) {
			playTurnMessage.setTitle(MessageTitle.GAME_END);
			addGameToDataBase(table, playedTurn, GameResultType.WIN_BY_PLAY);
		} else {
			playTurnMessage.setTitle(MessageTitle.POOL_PLAY_TURN);
		}
		PoolMessage poolMessage = createPoolMessage(playedTurn);
		playTurnMessage.setPoolMessage(poolMessage);
		return playTurnMessage;
	}

	private PoolMessage createPoolMessage(PoolTurn playedTurn) {
		PoolMessage poolMessage = new PoolMessage();
		poolMessage.setCue(playedTurn.getCue());
		poolMessage.setCueBall(playedTurn.getCueBall());
		poolMessage.setWinner(playedTurn.getWinner());
		poolMessage.setTurnResult(playedTurn.getTurnResult().toString());
		poolMessage.setWhoPlayed(playedTurn.getPlayer());
		return poolMessage;
	}

	public Message makeComputerMove(Table table) {
		PoolTable poolTable = (PoolTable) table;
		PoolAI poolAI = (PoolAI) table.getPlayerInTurn();
		if (poolTable.isExpectingHandball()) {
			PoolTurn playedTurn = poolAI.findHandBallPlace(poolTable, poolAI);
			playedTurn.setTurnType(TurnType.HANDBALL);
			PoolMessage poolMessage = new PoolMessage();
			poolMessage.setCueBall(poolTable.getCueBall());
			poolMessage.setTurnResult(playedTurn.getTurnResult().toString());
			Message handBallMessage = new Message();
			handBallMessage.setFrom(SYSTEM);
			handBallMessage.setTable(table);
			handBallMessage.setTitle(MessageTitle.POOL_HANDBALL);
			handBallMessage.setPoolMessage(poolMessage);
			PoolTurn incomingTurn = new PoolTurn();
			incomingTurn.setCueBall(playedTurn.getCueBall());
			// CueBall position from what was accepted by backend within "incomingTurn"
			addTurnToDatabase(incomingTurn, poolTable, playedTurn, poolAI, TurnType.HANDBALL);
			return handBallMessage;
		}
		if (poolTable.isExpectingPocketSelection()) {
			PoolTurn selectedPocketTurn = poolTable.selectPocket(poolAI, Integer.valueOf(1));
			PoolMessage poolMessage = new PoolMessage();
			poolMessage.setCueBall(poolTable.getCueBall());
			poolMessage.setSelectedPocket(selectedPocketTurn.getSelectedPocket());
			poolMessage.setTurnResult(selectedPocketTurn.getTurnResult().toString());
			Message selectedPocketMessage = new Message();
			selectedPocketMessage.setFrom(SYSTEM);
			selectedPocketMessage.setTable(table);
			selectedPocketMessage.setTitle(MessageTitle.POOL_SELECT_POCKET);
			selectedPocketMessage.setPoolMessage(poolMessage);
			addTurnToDatabase(selectedPocketTurn, poolTable, selectedPocketTurn, poolAI, TurnType.POCKET_SELECTION);
			return selectedPocketMessage;
		}
		PoolTurn turnToBePlayed = poolAI.createTurn(poolTable);
		PoolTurn playedTurn = (PoolTurn) table.playTurn(poolAI, turnToBePlayed);
		Message message = createPlayedTurnMessage(poolTable, playedTurn);
		addTurnToDatabase(turnToBePlayed, poolTable, playedTurn, poolAI, TurnType.PLAY);
		return message;
	}

	private void addGameToDataBase(PoolTable table, PoolTurn resultingTurn, GameResultType gameResultType) {
		GameResult gameResult = new GameResult();
		gameResult.setGameMode(table.getGameMode());
		gameResult.setStartInstant(table.getStartTime());
		gameResult.setPlayerA(table.getPlayerA());
		gameResult.setPlayerB(table.getPlayerB());
		gameResult.setWinner(resultingTurn.getWinner());
		gameResult.setResultType(gameResultType);
		GameStatisticsEvent statsEvent = new GameStatisticsEvent();
		statsEvent.setGameResult(gameResult);
		statisticsEvent.fireAsync(statsEvent);
	}

	private void addTurnToDatabase(PoolTurn incomingTurn, PoolTable table, PoolTurn resultingTurn, User usera, TurnType type) {
		try {
			String user = usera.getName();
			String userId = usera.getId() == null ? user : usera.getId().toString();
			String winnerName = resultingTurn.getWinner() == null ? null : resultingTurn.getWinner().getName();
			String winnerId = resultingTurn.getWinner() == null ? winnerName : resultingTurn.getWinner().getId() == null ? winnerName : resultingTurn.getWinner().getId().toString();
			Double cueForce = incomingTurn.getCue() == null ? null : incomingTurn.getCue().getForce();
			Double cueAngle = incomingTurn.getCue() == null ? null : incomingTurn.getCue().getAngle();
			Double cueX = incomingTurn.getCueBall() == null ? null : incomingTurn.getCueBall().getPosition().x;
			Double cueY = incomingTurn.getCueBall() == null ? null : incomingTurn.getCueBall().getPosition().y;
			Integer selectedPocket = resultingTurn.getSelectedPocket() == null ? null : resultingTurn.getSelectedPocket();
			PoolTurnStats result = new PoolTurnStats.PoolTurnBuilder().force(cueForce).angle(cueAngle).cueBallX(cueX).cueBallY(cueY)
					.remainingBalls(table.getRemainingBalls().stream().map(ball -> String.valueOf(ball.getNumber())).collect(Collectors.joining(","))).playerId(userId).playerName(user).turnResult(resultingTurn.getTurnResult().toString())
					.gameId(table.getGameId()).winner(winnerName).winnerId(winnerId).selectedPocket(selectedPocket).turnType(type.getAsText()).build();
			PoolGameStatisticsEvent statsEvent = new PoolGameStatisticsEvent();
			statsEvent.setGameResult(result);
			fireStatisticsEvent(result);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Pool turnUpdate to db failed:" + e);
		}
	}

	private void fireStatisticsEvent(PoolTurnStats turnResult) {
		PoolGameStatisticsEvent statsEvent = new PoolGameStatisticsEvent();
		statsEvent.setGameResult(turnResult);
		poolStatsEvent.fireAsync(statsEvent);
	}



}
