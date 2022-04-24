package com.tauhka.games.pool;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import com.tauhka.games.core.User;
import com.tauhka.games.core.Vector2d;
import com.tauhka.games.core.ai.AI;

/**
 * @author antsa-1 from GitHub 15 Apr 2022
 **/

public class PoolAI extends User implements AI {

	public PoolTurn createTurn(PoolTable table) {

		Ball targetType = determineTargetType(table);
		CueBall cueBall = table.getCueBall();
		Optional<Ball> optional = table.getRemainingBalls().stream().filter(balla -> balla.isSimilar(targetType)).findFirst();
		if (optional.isPresent()) {
			Ball ownBall = optional.get();
			return createTurn(cueBall, ownBall);
		}
		// Only eight ball left
		Optional<Ball> eightBall = table.getRemainingBalls().stream().filter(balla -> balla.getNumber() == 8).findFirst();
		return createTurn(cueBall, eightBall.get());
	}

	private PoolTurn createTurn(CueBall cueBall, Ball ownBall) {
		double angle = calculateAngle(cueBall, ownBall);
		Cue cue = new Cue();
		cue.setAngle(angle);
		cue.setForce(225d);
		PoolTurn turn = new PoolTurn();
		turn.setCue(cue);
		turn.setTurnType(TurnType.PLAY);
		return turn;
	}

	private double calculateAngle(CueBall cueBall, Ball ownBall) {
		double x = ownBall.getPosition().x - cueBall.getPosition().x;
		double y = ownBall.getPosition().y - cueBall.getPosition().y;
		double angle = Math.atan2(y, x);
		return angle;
	}

	private Ball determineTargetType(PoolTable table) {
		List<Ball> balls = table.getPlayerInTurnBalls();
		if (balls.size() > 0) {
			return balls.get(0);
		}
		List<Ball> opponentBalls = table.getPlayerNotInTurnBalls();
		if (opponentBalls.size() > 0) {
			Ball opponentBall = opponentBalls.get(0);
			if (opponentBall.isLower()) {
				Ball ball = new Ball();
				ball.setColor(Color.YELLOW);
				return ball;
			}
			Ball ball = new Ball();
			ball.setColor(Color.RED);
			return ball;
		}
		// Open table
		int random = ThreadLocalRandom.current().nextInt(1, 1001);
		Ball ball = new Ball();
		if (random < 501) {
			ball.setColor(Color.RED);
		} else {
			ball.setColor(Color.YELLOW);
		}
		return ball;
	}

	public PoolTurn findHandBallPlace(PoolTable poolTable, PoolAI poolAI) {
		CueBall cueBall = new CueBall();
		Double x = 400d;
		Double y = 300d;
		if (!poolTable.isBreaked()) {
			x = ThreadLocalRandom.current().nextDouble(225, 290);
			y = ThreadLocalRandom.current().nextDouble(225, 375);
		}
		Vector2d position = new Vector2d(x, y);
		cueBall.setPosition(position);
		PoolTurn turn = null;
		while (poolTable.isExpectingHandball()) {
			if (poolTable.isBreaked()) {
				x++;
			}
			y++;
			position = new Vector2d(x, y);
			cueBall.setPosition(position);
			turn = poolTable.updateHandBall(poolAI, cueBall);
			if (x > 1000 || y > 1000) {
				throw new RuntimeException("Could not find handball place, fail");
			}
		}
		return turn;
	}
}
