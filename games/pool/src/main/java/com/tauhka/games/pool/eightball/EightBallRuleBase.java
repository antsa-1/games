package com.tauhka.games.pool.eightball;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.tauhka.games.core.Vector2d;
import com.tauhka.games.core.util.VectorUtil;
import com.tauhka.games.pool.Ball;
import com.tauhka.games.pool.Boundry;
import com.tauhka.games.pool.Cue;
import com.tauhka.games.pool.CueBall;
import com.tauhka.games.pool.PathWay;
import com.tauhka.games.pool.Pocket;
import com.tauhka.games.pool.PoolTable;
import com.tauhka.games.pool.PoolTurn;
import com.tauhka.games.pool.TurnResult;

/**
 * @author antsa-1 from GitHub 28 Mar 2022
 **/

/* https://openjdk.java.net/jeps/417 JEP 417: Vector API (Third Incubator) any help here with jdk 18? */
public class EightBallRuleBase {
	private static final Logger LOGGER = Logger.getLogger(EightBallRuleBase.class.getName());
	private final double DELTA = 0.125d;
	private final double FRICTION = 0.991d;
	private TurnResult turnResult;
	private int iterationCount = 0;
	private List<Ball> removalBalls = new ArrayList<Ball>();
	private boolean eightBallHitFirst = false;
	private boolean ownBallHitFirst = false;
	private boolean ownBallInPocket = false;
	private boolean opponentBallInPocket = false;
	private boolean cueBallInPocket = false;
	private boolean eightBallInPocket = false;

	public TurnResult playTurn(PoolTable table, PoolTurn turn) {
		this.reset();
		Cue cue = turn.getCue();
		if (cue.getForce() < 10) {
			LOGGER.severe("Cue force was set below minimum 10");
			cue.setForce(10d);
		} else if (cue.getForce() > 250) {
			LOGGER.severe("Cue force was set over maximum:" + cue.getForce());
			cue.setForce(250d);
		}
		double angle = cue.getAngle();
		if (Math.abs(angle) > Math.PI * 2) {
			LOGGER.severe("Cue force was set over maximum:" + cue.getForce());
			cue.setAngle(Math.PI);
		}
		Vector2d v = VectorUtil.calculateCueBallInitialVelocity(cue.getForce(), cue.getAngle());
		// LOGGER.info("CueBall initial velocity:" + v + " angle:" + cue.getAngle() + " force:" + cue.getForce());
		table.getCueBall().setVelocity(v);
		handleBallsMovements(table);
		for (Ball ball : removalBalls) {
			table.getRemainingBalls().remove(ball);
		}
		determineTurnResult(table);
		// table.updateCueBallPosition()
		return turnResult != null ? turnResult : TurnResult.CHANGE_TURN;
	}

	private TurnResult determineTurnResult(PoolTable table) {
		if (table.isOpen() && eightBallHitFirst && removalBalls.size() > 0 && turnResult == null) {
			LOGGER.info("EightBall tableOpen changeTurn");
			turnResult = TurnResult.CHANGE_TURN;
		}
		if (!ownBallHitFirst && turnResult == null) {
			// No ball was hit
			turnResult = TurnResult.HANDBALL;
		}
		if (turnResult == null && cueBallInPocket) {
			turnResult = TurnResult.HANDBALL;
		}
		if (turnResult == null && opponentBallInPocket) {
			turnResult = TurnResult.CHANGE_TURN;
		}
		if (turnResult == null && ownBallInPocket) {
			turnResult = TurnResult.CONTINUE_TURN;
		}

		// CueBall might roll to pocket as last ball
		if (eightBallInPocket && cueBallInPocket) {
			turnResult = TurnResult.EIGHT_BALL_IN_POCKET_FAIL;
		}
		if (eightBallInPocket && table.getPlayerInTurnBalls().size() != 7) {
			turnResult = TurnResult.EIGHT_BALL_IN_POCKET_FAIL;
		}
		if (eightBallInPocket && turnResult == TurnResult.HANDBALL) {
			turnResult = TurnResult.EIGHT_BALL_IN_POCKET_FAIL;
		}
		if (eightBallInPocket && !eightBallHitFirst) {
			turnResult = TurnResult.EIGHT_BALL_IN_POCKET_FAIL;
		}

		return turnResult != null ? turnResult : TurnResult.CHANGE_TURN;
	}

	private void reset() {
		turnResult = null;
		iterationCount = 0;
		removalBalls = new ArrayList<Ball>();
		eightBallHitFirst = false;
		ownBallInPocket = false;
		ownBallHitFirst = false;
		opponentBallInPocket = false;
		cueBallInPocket = false;
		eightBallInPocket = false;
	}

	public boolean isCueBallNewPositionAllowed(PoolTable table, CueBall cueBall) {
		Boundry left = table.getBoundries().get(5);
		Boundry right = table.getBoundries().get(2);
		Boundry topLeft = table.getBoundries().get(0);
		Boundry bottomRight = table.getBoundries().get(3);
		double x = cueBall.getPosition().x;
		double y = cueBall.getPosition().y;
		double r = cueBall.getRadius();
		if (!table.isBreaked() && x > 299) { // hardcoded value. it will be a different story to make the table responsive
			LOGGER.info("Wrong handball position during break... UI check, todo?");
			return false;
		}
		if (x > left.a + r && x < right.a - r) {
			if (!(y > topLeft.a + r && y < bottomRight.a - r)) {
				return false;
			}
		} else {
			return false;
		}
		for (Ball ball : table.getRemainingBalls()) {
			if (ball.getNumber() == 0) {
				continue;
			}
			if (areBallsIntersecting(ball, cueBall)) {
				return false;
			}
		}
		return true;
	}

	public boolean areBallsIntersecting(Ball ball, Ball cueBall) {
		double x = Math.abs(ball.getPosition().x - cueBall.getPosition().x);
		if (x > ball.getDiameter()) {
			return false;
		}
		double y = Math.abs(ball.getPosition().y - cueBall.getPosition().y);
		return y < ball.getDiameter();
	}

	private void handleBallsMovements(PoolTable table) {
		while (true) {
			updatePositionsAndApplyFriction(table);
			detectCollisions(table);
			if (!isAnyBallMoving(table.getRemainingBalls())) {
				break;
			}
			/* try { table.notify(); table.wait(); } catch (Exception e) { // TODO Auto-generated catch block e.printStackTrace(); } */
		}
	}

	private void updatePositionsAndApplyFriction(PoolTable table) {
		for (Ball ball : table.getRemainingBalls()) {
			if (ball.isInPocket()) {
				ball.getVelocity().x = 0;
				ball.getVelocity().y = 0;
				continue;
			}
			Vector2d changeInPosition = VectorUtil.multiply(ball.getVelocity(), DELTA);
			Vector2d currentPosition = ball.getPosition();
			ball.setPosition(VectorUtil.addVectors(changeInPosition, currentPosition));
			Vector2d newVelocity = VectorUtil.multiply(ball.getVelocity(), FRICTION);
			ball.setVelocity(newVelocity);
		}
	}

	private boolean isAnyBallMoving(List<Ball> balls) {
		boolean retVal = false;
		for (Ball b : balls) {
			if (b.isMoving()) {
				retVal = true;
			}
			VectorUtil.calculateAndHandleVectorLength(b.getVelocity());
			// this returns immediately if match is found -> not every balls velocity is calculated/changed.. potential issue but UI-works the same atm.
			if (!b.getVelocity().isZero()) {
				return true;
			}

		}
		return retVal;
	}

	private void detectCollisions(PoolTable table) {
		for (int i = 0; i < table.getRemainingBalls().size(); i++) {
			Ball ball = table.getRemainingBalls().get(i);
			if (ball.isInPocket()) {
				continue;
			}
			if (ball.isMoving()) {
				checkAndHandleTableCollisions(table, ball);
			}
			checkAndHandleBallOnBallCollisions(table, i);
		}
	}

	private void checkAndHandleBallOnBallCollisions(PoolTable table, int i) {
		for (int j = i + 1; j < table.getRemainingBalls().size(); j++) {
			Ball firstBall = table.getRemainingBalls().get(i);
			Ball secondBall = table.getRemainingBalls().get(j);
			if (firstBall.isMoving() || secondBall.isMoving()) {
				collide(table, firstBall, secondBall);
			}
		}
	}

	private boolean isAllowedToHit(PoolTable table, Ball ball) {
		if (table.isOpen()) {
			// No selection of balls has been done
			return true;
		}
		if (ball.isEightBall()) {
			return table.getPlayerInTurnBalls().size() == 7 ? true : false;
		}
		Ball playerABall = table.getPlayerABalls().isEmpty() ? null : table.getPlayerABalls().get(0);
		Ball playerBBall = table.getPlayerBBalls().isEmpty() ? null : table.getPlayerBBalls().get(0);
		if (playerABall == null && playerBBall == null) {
			return true;
		}
		if (table.getPlayerInTurnBalls().size() > 0) {
			return ball.isSimilar(table.getPlayerInTurnBalls().get(0));
		}
		if (table.getPlayerNotInTurnBalls().size() > 0) {
			return !ball.isSimilar(table.getPlayerNotInTurnBalls().get(0));
		}
		return true;
	}

	/* Mathematics from -> ' JavaScript + HTML5 GameDev Tutorial: 8-Ball Pool Game (part 2) ' from 15:42 -> // https://www.youtube.com/watch?v=Am8rT9xICRs */
	private void collide(PoolTable table, Ball firstBall, Ball secondBall) {
		if (firstBall.isInPocket() || secondBall.isInPocket()) {
			return;
		}
		Vector2d normalVector = VectorUtil.subtractVectors(firstBall.getPosition(), secondBall.getPosition());

		double normalVectorLength = VectorUtil.calculateVectorLength(normalVector);
		if (normalVectorLength > firstBall.getDiameter()) {
			// LOGGER.info("No collision between:" + firstBall + " and " + secondBall);
			return;
		}
		if (iterationCount == 0) { // Cue ball should be secondBall in the first round
			table.setBreaked(true);
			if (firstBall.isEightBall()) {
				this.eightBallHitFirst = true;
			}
			if (!isAllowedToHit(table, firstBall)) {
				this.turnResult = TurnResult.HANDBALL;
			} else {
				this.ownBallHitFirst = true;
			}
			iterationCount++;
		}
		Vector2d mtd = VectorUtil.multiply(normalVector, (firstBall.getDiameter() - normalVectorLength) / normalVectorLength);
		firstBall.getPosition().x += mtd.x * 0.5;
		firstBall.getPosition().y += mtd.y * 0.5;
		secondBall.getPosition().x -= mtd.x * 0.5;
		secondBall.getPosition().y -= mtd.y * 0.5;
		double scalar = 1 / normalVectorLength;
		Vector2d unitNormalVector = VectorUtil.multiply(normalVector, scalar);
		Vector2d unitTangentVector = new Vector2d(-unitNormalVector.y, unitNormalVector.x);
		double v1n = VectorUtil.dotProduct(unitNormalVector, firstBall.getVelocity());
		double v1t = VectorUtil.dotProduct(unitTangentVector, firstBall.getVelocity());
		double v2n = VectorUtil.dotProduct(unitNormalVector, secondBall.getVelocity());
		double v2t = VectorUtil.dotProduct(unitTangentVector, secondBall.getVelocity());
		Vector2d v1nTag = VectorUtil.multiply(unitNormalVector, v2n);
		Vector2d v1tTag = VectorUtil.multiply(unitTangentVector, v1t);
		Vector2d v2nTag = VectorUtil.multiply(unitNormalVector, v1n);
		Vector2d v2tTag = VectorUtil.multiply(unitTangentVector, v2t);
		Vector2d firstBallVelocity = new Vector2d(v1nTag.x + v1tTag.x, v1nTag.y + v1tTag.y);
		firstBall.setVelocity(firstBallVelocity);
		Vector2d secondBallVelocity = new Vector2d(v2nTag.x + v2tTag.x, v2nTag.y + v2tTag.y);
		secondBall.setVelocity(secondBallVelocity);
	}

	private void checkAndHandleTableCollisions(PoolTable table, Ball ball) {
		if (checkAndHandlePockets(table, ball) == true) {
			// Ball is now put in middle of pocket
			return;
		}
		if (isInMiddleArea(table, ball)) {
			return;
		}
		if (checkAndHandleTableBoundriesCollisions(table, ball)) {
			if (ball.getNumber() == 11) {
			}
			return;
		}
		if (checkAndHandlePocketPathwayCollisions(table, ball)) {
			return;
		}

	}

	private void removeFromNextTurn(Ball ball) {
		if (ball.getNumber() == 0) {
			return;
		}
		// vs. compare what is in the pockets after turn.. this is run many time
		if (!removalBalls.contains(ball)) {
			removalBalls.add(ball);
		}
	}

	private boolean checkAndHandlePockets(PoolTable table, Ball balla) {
		boolean ballPutInPocket = false;
		for (Pocket pocket : table.getPockets()) {
			if (!isBallInPocket(table, balla, pocket)) {
				continue;
			}
			LOGGER.info("Ball:" + balla + " goes In Pocket:" + pocket);

			balla.getVelocity().x = 0d;
			balla.getVelocity().y = 0d;
			balla.setPosition(pocket.getCenter());
			int ownBalls = table.getPlayerInTurnBalls().size();
			int opponentBalls = table.getPlayerNotInTurnBalls().size();
			table.putBallInPocket(balla, pocket);
			ballPutInPocket = true;
			if (balla.getNumber() == 8) {
				eightBallInPocket = true;
				// This should check the order of balls going in pocket if several balls at the same time goes
				if (table.getPlayerInTurnBalls().size() != 7) {
					this.turnResult = TurnResult.EIGHT_BALL_IN_POCKET_FAIL;
				} else if (table.getSelectedPocket() == null || !table.getSelectedPocket().hasEightBall()) {
					this.turnResult = TurnResult.EIGHT_BALL_IN_POCKET_FAIL;
				} else {
					this.turnResult = TurnResult.EIGHT_BALL_IN_POCKET_OK;
				}
			} else if (balla.getNumber() == 0) {
				this.cueBallInPocket = true;

			}
			if (table.getPlayerInTurnBalls().size() > ownBalls) {
				this.ownBallInPocket = true;
			}
			if (table.getPlayerNotInTurnBalls().size() > opponentBalls) {
				this.opponentBallInPocket = true;
			}
			removeFromNextTurn(balla);
			break;
		}
		return ballPutInPocket;

	}

	private boolean isBallInPocket(PoolTable table, Ball ball, Pocket pocket) {
		// // https://stackoverflow.com/questions/481144/equation-for-testing-if-a-point-is-inside-a-circle
		double dx = Math.abs(ball.getPosition().x - pocket.getCenter().x);
		if (dx > pocket.getRadius()) {
			return false;
		}
		double dy = ball.getPosition().y - pocket.getCenter().y;
		if (dy > pocket.getRadius()) {
			return false;
		}
		if (Math.pow(dx, 2) + Math.pow(dy, 2) <= Math.pow(pocket.getRadius(), 2)) {
			return true;
		}
		return false;
	}

	private boolean checkAndHandleTableBoundriesCollisions(PoolTable table, Ball ball) {
		Boundry topLeft = table.getBoundries().get(0);
		Boundry topRight = table.getBoundries().get(1);
		Boundry right = table.getBoundries().get(2);
		Boundry bottomRight = table.getBoundries().get(3);
		Boundry bottomLeft = table.getBoundries().get(4);
		Boundry left = table.getBoundries().get(5);

		double radius = ball.getDiameter() / 2;
		if (isTableTopBondry(topLeft, topRight, ball.getPosition(), radius)) {
			ball.getVelocity().y = Math.abs(ball.getVelocity().y);
			return true;
		}
		if (isTableBottomBoundry(bottomLeft, bottomRight, ball.getPosition(), radius)) {
			ball.getVelocity().y = -Math.abs(ball.getVelocity().y);
			return true;
		}
		if (isTableLeftBoundry(left, ball.getPosition(), radius)) {
			ball.getVelocity().x = Math.abs(ball.getVelocity().x);
			return true;
		}
		if (isTableRightBoundry(right, ball.getPosition(), radius)) {
			ball.getVelocity().x = -Math.abs(ball.getVelocity().x);
			return true;
		}

		return false;
	}

	private boolean checkAndHandlePocketPathwayCollisions(PoolTable table, Ball ball) {
		for (Pocket pocket : table.getPockets()) {
			if (this.isPathwayBorderCollision(table, pocket.getPathWayRight(), ball)) {
				Vector2d reflectionVector = calculateBallVelocityOnPathwayBorderCollision(table, pocket.getPathWayRight(), ball);
				ball.setVelocity(reflectionVector);
				return true;
			} else if (this.isPathwayBorderCollision(table, pocket.getPathwayLeft(), ball)) {
				Vector2d reflectionVector = calculateBallVelocityOnPathwayBorderCollision(table, pocket.getPathwayLeft(), ball);
				ball.setVelocity(reflectionVector);
				return true;
			}
		}
		return false;
	}

	private boolean isPathwayBorderCollision(PoolTable table, PathWay pathway, Ball ball) {
		// https://stackoverflow.com/questions/1073336/circle-line-segment-collision-detection-algorithm -> answer starting "No one seems to consider projection ..."
		Vector2d a = pathway.getTop();
		Vector2d b = pathway.getBottom();
		Vector2d c = ball.getPosition();
		Vector2d ac = VectorUtil.subtractVectors(c, a);
		Vector2d ab = VectorUtil.subtractVectors(b, a);
		Vector2d projectedVector = VectorUtil.projectVectorOnVector(ac, ab);
		Vector2d d = VectorUtil.addVectors(projectedVector, a);
		Vector2d ad = VectorUtil.subtractVectors(d, a);

		double k = Math.abs(ab.x) > Math.abs(ab.y) ? ad.x / ab.x : ad.y / ab.y;
		Double distance = null;
		if (k <= 0.0) {
			distance = Math.sqrt(VectorUtil.hypot2(c, a));
		} else if (k >= 1.0) {
			distance = Math.sqrt(VectorUtil.hypot2(c, b));
		}
		if (distance == null) {
			distance = Math.sqrt(VectorUtil.hypot2(c, d));
		}

		return distance <= ball.getRadius();

	}

	private Vector2d calculateBallVelocityOnPathwayBorderCollision(PoolTable table, PathWay pathWay, Ball ball) {
		// https://stackoverflow.com/questions/61272597/calculate-the-bouncing-angle-for-a-ball-point
		Vector2d normalVector = VectorUtil.subtractVectors(pathWay.getBottom(), pathWay.getTop());
		double normalVectorLength = VectorUtil.calculateVectorLength(normalVector);
		double scalar = 1 / normalVectorLength;
		Vector2d unitNormalVector = VectorUtil.multiply(normalVector, scalar);
		double magnitude = 2 * VectorUtil.dotProduct(unitNormalVector, ball.getVelocity());
		Vector2d multiplied = VectorUtil.multiply(unitNormalVector, magnitude);
		Vector2d reflectionVector = VectorUtil.subtractVectors(multiplied, ball.getVelocity());
		return reflectionVector;
	}

	private boolean isTableTopBondry(Boundry topLeft, Boundry topRight, Vector2d ballPosition, double radius) {
		return ballPosition.x >= topLeft.b && ballPosition.x <= topLeft.c && ballPosition.y - radius <= topLeft.a || ballPosition.x >= topRight.b && ballPosition.x <= topRight.c && ballPosition.y - radius <= topRight.a;
	}

	public boolean isTableLeftBoundry(Boundry left, Vector2d ballPosition, double radius) {
		return ballPosition.x <= left.a + radius && ballPosition.y >= left.b && ballPosition.y <= left.c;
	}

	public boolean isTableRightBoundry(Boundry right, Vector2d ballPosition, double radius) {
		return ballPosition.x >= right.a - radius && ballPosition.y >= right.b && ballPosition.y <= right.c;
	}

	public boolean isTableBottomBoundry(Boundry bottomLeft, Boundry bottomRight, Vector2d ballPosition, double radius) {
		return ballPosition.x >= bottomLeft.b && ballPosition.x <= bottomLeft.c && ballPosition.y + radius >= bottomLeft.a || ballPosition.x >= bottomRight.b && ballPosition.x <= bottomRight.c && ballPosition.y + radius >= bottomRight.a;
	}

	public boolean isInMiddleArea(PoolTable table, Ball b) {

		Vector2d topLeft = table.getMiddleAreaStart();
		Vector2d bottomRight = table.getMiddleAreaEnd();
		if (b.getPosition().x > topLeft.x && b.getPosition().x < bottomRight.x) {
			return b.getPosition().y > topLeft.y && b.getPosition().y < bottomRight.y;
		}
		return false;
	}

}
