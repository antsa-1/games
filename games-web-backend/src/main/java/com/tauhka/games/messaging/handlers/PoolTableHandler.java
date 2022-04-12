package com.tauhka.games.messaging.handlers;

import static com.tauhka.games.core.util.Constants.SYSTEM;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.tauhka.games.core.stats.PoolGameStatisticsEvent;
import com.tauhka.games.core.tables.Table;
import com.tauhka.games.core.twodimen.PoolTurnStats;
import com.tauhka.games.messaging.Message;
import com.tauhka.games.messaging.MessageTitle;
import com.tauhka.games.messaging.PoolMessage;
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
public class PoolTableHandler {
	private static final Logger LOGGER = Logger.getLogger(PoolTableHandler.class.getName());
	@Inject
	private Event<PoolGameStatisticsEvent> statisticsEvent;

	public Message updateCuePosition(CommonEndpoint endpoint, Message message) {
		Table table = findUserTable(endpoint);

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
		updateCueMessage.setCueBall(message.getPoolMessage().getCueBall());
		updateCueMessage.setCanvas(message.getPoolMessage().getCanvas());
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
		// selectPocketMessage.setCueBall(message.getPoolMessage().getCueBall());
		// selectPocketMessage.setCanvas(message.getPoolMessage().getCanvas());
		updateMessage.setPoolMessage(selectPocketMessage);
		createTurnToDatabase(incomingTurn, table, selectedPocketTurn, endpoint, TurnType.POCKET_SELECTION);
		return updateMessage;
	}

	public Message updateHandBall(CommonEndpoint endpoint, Message message) {
		PoolTable table = (PoolTable) findUserTable(endpoint);
		PoolTurn incomingTurn = new PoolTurn();
		incomingTurn.setCanvas(message.getPoolMessage().getCanvas());
		incomingTurn.setCue(message.getPoolMessage().getCue());
		incomingTurn.setCueBall(message.getPoolMessage().getCueBall());
		PoolTurn turn = table.updateHandBall(endpoint.getUser(), message.getPoolMessage().getCueBall());
		Message updateMessage = new Message();
		updateMessage.setFrom(SYSTEM);
		updateMessage.setTable(table);
		updateMessage.setTitle(MessageTitle.POOL_HANDBALL);
		PoolMessage updateCueBallPositionMessage = new PoolMessage();
		updateCueBallPositionMessage.setCueBall(message.getPoolMessage().getCueBall());
		updateCueBallPositionMessage.setCanvas(message.getPoolMessage().getCanvas());
		updateCueBallPositionMessage.setTurnResult(turn.getTurnResult());
		updateMessage.setPoolMessage(updateCueBallPositionMessage);
		createTurnToDatabase(incomingTurn, table, turn, endpoint, TurnType.HANDBALL);
		return updateMessage;
	}

	public Message playTurn(CommonEndpoint endpoint, Message message) {
		// ServerSide calcs?
		PoolTurn incomingTurn = new PoolTurn();
		incomingTurn.setCanvas(message.getPoolMessage().getCanvas());
		incomingTurn.setCue(message.getPoolMessage().getCue());
		incomingTurn.setCueBall(message.getPoolMessage().getCueBall());
		PoolTable table = (PoolTable) findUserTable(endpoint);
		PoolTurn playedTurn = (PoolTurn) table.playTurn(endpoint.getUser(), incomingTurn);
		Message playTurnMessage = new Message();
		playTurnMessage.setFrom(SYSTEM);
		playTurnMessage.setTable(table);
		if (playedTurn.getTurnResult().equals(TurnResult.EIGHT_BALL_IN_POCKET_OK.toString()) || playedTurn.getTurnResult().equals(TurnResult.EIGHT_BALL_IN_POCKET_FAIL.toString())) {
			playTurnMessage.setTitle(MessageTitle.GAME_END);
		} else {
			playTurnMessage.setTitle(MessageTitle.POOL_PLAY_TURN);
		}
		PoolMessage poolMessage = new PoolMessage();
		poolMessage.setCue(playedTurn.getCue());
		poolMessage.setCueBall(playedTurn.getCueBall());
		poolMessage.setWinner(playedTurn.getWinner());
		poolMessage.setTurnResult(playedTurn.getTurnResult().toString());
		playTurnMessage.setPoolMessage(poolMessage);
		createTurnToDatabase(incomingTurn, table, playedTurn, endpoint, TurnType.PLAY);
		return playTurnMessage;
	}

	private void createTurnToDatabase(PoolTurn incomingTurn, PoolTable table, PoolTurn resultingTurn, CommonEndpoint endpoint, TurnType type) {
		try {
			String user = endpoint.getUser().getName();
			String userId = endpoint.getUser().getId() == null ? endpoint.getUser().getName() : endpoint.getUser().getId().toString();
			String winnerName = resultingTurn.getWinner() == null ? null : resultingTurn.getWinner().getName();
			String winnerId = resultingTurn.getWinner() == null ? winnerName : resultingTurn.getWinner().getId() == null ? winnerName : resultingTurn.getWinner().getId().toString();
			Double cueForce = incomingTurn.getCue() == null ? null : incomingTurn.getCue().getForce();
			Double cueAngle = incomingTurn.getCue() == null ? null : incomingTurn.getCue().getAngle();
			Double cueX = incomingTurn.getCueBall() == null ? null : incomingTurn.getCueBall().getPosition().x;
			Double cueY = incomingTurn.getCueBall() == null ? null : incomingTurn.getCueBall().getPosition().y;
			Integer selectedPocket = resultingTurn.getSelectedPocket() == null ? -1 : resultingTurn.getSelectedPocket();
			PoolTurnStats result = new PoolTurnStats.PoolTurnBuilder(cueForce, cueAngle).cueBallX(cueX).cueBallY(cueY)
					.remainingBalls(table.getRemainingBalls().stream().map(ball -> String.valueOf(ball.getNumber())).collect(Collectors.joining(","))).playerId(userId).playerName(user).turnResult(resultingTurn.getTurnResult().toString())
					.gameId(table.getTableId()).winner(winnerName).winnerId(winnerId).selectedPocket(selectedPocket).turnType(type.getAsText()).build();
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
		statisticsEvent.fireAsync(statsEvent);
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
