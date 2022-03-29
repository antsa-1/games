package com.tauhka.games.pool;

import java.util.logging.Logger;

import com.tauhka.games.core.Vector2d;
import com.tauhka.games.core.util.VectorUtil;

/**
 * @author antsa-1 from GitHub 28 Mar 2022
 **/

public class EightBallRuleBase {
	private final double DELTA = 0.125d;
	private final double FRICTION = 0.991d;
	private static final Logger LOGGER = Logger.getLogger(EightBallRuleBase.class.getName());

	public void handleMovements(PoolTable table, PoolTurn turn) {
		Cue cue = turn.getCue();
		Vector2d v = VectorUtil.calculateCueBallInitialVelocity(cue.getForce(), cue.getAngle());
		LOGGER.info("CueBall initial velocity:" + v);
		table.getCueBall().setVelocity(v);
		boolean ballsMoving = true;
		while (ballsMoving) {
			ballsMoving = calculateBallsMovements(table);
		}
	}

	private boolean calculateBallsMovements(PoolTable table) {
		boolean ballsMoving = false;
		for (Ball ball : table.getRemainingBalls()) {
			LOGGER.info("RuleBase -> Ball: " + ball.getNumber() + " velocity:" + ball.getVelocity() + " position: " + ball.getPosition());
			if (ball.getVelocity().isZero()) {
				LOGGER.fine("RuleBase -> Ball: " + ball.getNumber() + " had no velocity");
				continue; // Ball is not on the move
			}
			Vector2d tempVector = new Vector2d(ball.getVelocity().x, ball.getVelocity().y);
			VectorUtil.multiplyVector(tempVector, DELTA);
			Vector2d v = VectorUtil.addVectors(ball.getPosition(), tempVector);
			ball.setPosition(v);
			VectorUtil.multiplyVector(ball.getVelocity(), FRICTION);
			double length = VectorUtil.calculateVectorLength(ball.getVelocity());
			LOGGER.info("RuleBase after -> Ball: " + ball.getNumber() + ball.getVelocity() + " position: " + ball.getPosition());
			if (length == 0.0d) {
				ball.getVelocity().x = 0d;
				ball.getVelocity().y = 0d;
			} else {
				ballsMoving = true;
			}
		}
		LOGGER.info("RuleBase -> are balls still moving:" + ballsMoving);
		return ballsMoving;
	}
}
