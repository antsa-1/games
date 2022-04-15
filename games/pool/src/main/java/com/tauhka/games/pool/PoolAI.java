package com.tauhka.games.pool;

import java.util.List;
import java.util.Optional;

import com.tauhka.games.core.User;
import com.tauhka.games.core.Vector2d;
import com.tauhka.games.core.ai.AI;
import com.tauhka.games.core.util.VectorUtil;

/**
 * @author antsa-1 from GitHub 15 Apr 2022
 **/

public class PoolAI extends User implements AI {

	public PoolTurn playAITurn(PoolTable table) {

		Ball myBall = determineOwnBall(table);
		CueBall cueBall = table.getCueBall();
		Optional<Ball> optional = table.getRemainingBalls().stream().filter(balla -> balla.isSimilar(myBall)).findFirst();
		if (optional.isPresent()) {
			Ball ownBall = optional.get();
			return findAngleToBall(cueBall, ownBall);
		}
		if (table.getRemainingBalls().size() == 16) {
			// No selection has been done, take the first one
			Ball ownBall = table.getRemainingBalls().get(0);
			return findAngleToBall(cueBall, ownBall);
		}
		// Only eight ball left
		Optional<Ball> eightBall = table.getRemainingBalls().stream().filter(balla -> balla.getNumber() == 8).findFirst();
		return findAngleToBall(cueBall, eightBall.get());
	}

	private PoolTurn findAngleToBall(CueBall cueBall, Ball ownBall) {
		double angle = calculateAngle(cueBall, ownBall);
		Cue cue = new Cue();
		cue.setAngle(angle);
		cue.setForce(150d);
		PoolTurn turn = new PoolTurn();
		turn.setCue(cue);
		return turn;
	}

	private double calculateAngle(CueBall cueBall, Ball ownBall) {
		Vector2d subtractedPositions = VectorUtil.subtractVectors(cueBall.position, ownBall.getPosition());
		double angle = Math.atan2(subtractedPositions.y, subtractedPositions.x);
		return angle;
	}

	private Ball determineOwnBall(PoolTable table) {
		List<Ball> balls = table.getPlayerInTurnBalls();
		if (balls.size() > 0) {
			return balls.get(0);
		}
		List<Ball> opponentBalls = table.getPlayerNotInTurnBalls();
		if (opponentBalls.size() > 0) {
			Ball opponentBall = opponentBalls.get(0);
			if (opponentBall.isLower()) {
				Ball ball = new Ball();
				ball.setNumber(15);
				return ball;
			}
			Ball ball = new Ball();
			ball.setNumber(1);
			return ball;
		}
		return null;
	}
}
