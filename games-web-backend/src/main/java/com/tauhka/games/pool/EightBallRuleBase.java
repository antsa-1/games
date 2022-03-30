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
			ballsMoving = handleBallsMovements(table);
		}
	}

	private boolean handleBallsMovements(PoolTable table) {
		boolean ballsMoving = false;
		while (ballsMoving = updatePositions(table)) {
			detectCollisions(table);
		}
		// ballsMoving = detectCollisions(table);
		LOGGER.info("RuleBase handleBallsMovements done" + ballsMoving);
		return ballsMoving;
	}

	private boolean detectCollisions(PoolTable table) {
		// TODO Auto-generated method stub
		for (int i = 0; i < table.getRemainingBalls().size(); i++) {
			Ball ball = table.getRemainingBalls().get(i);
			if (ball.getNumber() == 0) {
				System.out.println("0");
			}
			// checkAndHandleTableCollisions(table);
			checkAndHandleBallOnBallCollisions(table, i);
		}
		return false;

	}

	private void checkAndHandleBallOnBallCollisions(PoolTable table, int i) {
		for (int j = i + 1; j < table.getRemainingBalls().size(); j++) {
			Ball firstBall = table.getRemainingBalls().get(i);
			Ball secondBall = table.getRemainingBalls().get(j);
			if (table.isInPocket(firstBall) || table.isInPocket(secondBall)) {
				break;
			}
			if (firstBall.getNumber() == 0) {
				System.out.println("00000");
			}
			if (firstBall.isMoving() || secondBall.isMoving()) {
				collide(firstBall, secondBall);
			}
		}
	}

	/* Mathematics from -> ' JavaScript + HTML5 GameDev Tutorial: 8-Ball Pool Game (part 2) ' from 15:42 -> // https://www.youtube.com/watch?v=Am8rT9xICRs */
	private void collide(Ball firstBall, Ball secondBall) {
		Vector2d normalVector = VectorUtil.subtractVectors(firstBall.getPosition(), secondBall.getPosition());
		double normalVectorLength = VectorUtil.calculateVectorLength(normalVector);
		if (normalVectorLength > firstBall.getDiameter()) {
			LOGGER.info("No collision between:" + firstBall + " and " + secondBall);
			return;
		}
		Vector2d mtd = VectorUtil.multiply(VectorUtil.copy(normalVector), (firstBall.getDiameter() - normalVectorLength) / normalVectorLength);
		firstBall.getPosition().x += mtd.x * 0.5;
		firstBall.getPosition().y += mtd.y * 0.5;
		secondBall.getPosition().x -= mtd.x * 0.5;
		secondBall.getPosition().y -= mtd.y * 0.5;
		double scalar = 1 / normalVectorLength;
		Vector2d unitNormalVector = VectorUtil.multiply(normalVector, scalar);
		Vector2d unitTangentVector = new Vector2d(-normalVector.y, normalVector.x);
		double v1n = VectorUtil.dotProduct(unitNormalVector, firstBall.getVelocity());
		double v1t = VectorUtil.dotProduct(unitTangentVector, firstBall.getVelocity());
		double v2n = VectorUtil.dotProduct(unitNormalVector, secondBall.getVelocity());
		double v2t = VectorUtil.dotProduct(unitTangentVector, secondBall.getVelocity());
		Vector2d v1nTag = VectorUtil.multiply(VectorUtil.copy(unitNormalVector), v2n);
		Vector2d v1tTag = VectorUtil.multiply(VectorUtil.copy(unitTangentVector), v1t);
		Vector2d v2nTag = VectorUtil.multiply(VectorUtil.copy(unitNormalVector), v1n);
		Vector2d v2tTag = VectorUtil.multiply(VectorUtil.copy(unitTangentVector), v2t);
		Vector2d firstBallVelocity = new Vector2d(v1nTag.x + v1tTag.x, v1nTag.y + v1tTag.y);
		firstBall.setVelocity(firstBallVelocity);
		Vector2d secondBallVelocity = new Vector2d(v2nTag.x + v2tTag.x, v2nTag.y + v2tTag.y);
		secondBall.setVelocity(secondBallVelocity);
	}

	private void checkAndHandleTableCollisions(PoolTable table) {
		// TODO Auto-generated method stub

	}

	private boolean updatePositions(PoolTable table) {
		boolean ballsStillMoving = false;
		for (Ball ball : table.getRemainingBalls()) {
			LOGGER.info("RuleBase -> Ball: " + ball.getNumber() + " velocity:" + ball.getVelocity() + " position: " + ball.getPosition());
			if (ball.getVelocity().isZero()) {
				LOGGER.fine("RuleBase -> Ball: " + ball.getNumber() + " had no velocity");
				continue; // Ball is not on the move
			}
			Vector2d tempVector = new Vector2d(ball.getVelocity().x, ball.getVelocity().y);
			VectorUtil.multiply(tempVector, DELTA);
			Vector2d v = VectorUtil.addVectors(ball.getPosition(), tempVector);
			ball.setPosition(v);
			VectorUtil.multiply(ball.getVelocity(), FRICTION);
			double length = VectorUtil.calculateVectorLength(ball.getVelocity());
			LOGGER.info("RuleBase after -> Ball: " + ball.getNumber() + ball.getVelocity() + " position: " + ball.getPosition());
			if (length == 0.0d) {
				ball.getVelocity().x = 0d;
				ball.getVelocity().y = 0d;
			} else {
				ballsStillMoving = true;
			}
		}
		return ballsStillMoving;
	}
}
