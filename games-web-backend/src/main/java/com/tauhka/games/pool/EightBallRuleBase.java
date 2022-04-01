package com.tauhka.games.pool;

import java.util.List;
import java.util.logging.Logger;

import com.tauhka.games.core.Vector2d;
import com.tauhka.games.core.util.VectorUtil;

/**
 * @author antsa-1 from GitHub 28 Mar 2022
 **/

/* https://openjdk.java.net/jeps/417 JEP 417: Vector API (Third Incubator) any help here with jdk 18? */
public class EightBallRuleBase {
	private final double DELTA = 0.125d;
	private final double FRICTION = 0.991d;
	private static final Logger LOGGER = Logger.getLogger(EightBallRuleBase.class.getName());

	public void handleMovements(PoolTable table, PoolTurn turn) {
		Cue cue = turn.getCue();
		Vector2d v = VectorUtil.calculateCueBallInitialVelocity(cue.getForce(), cue.getAngle());
		LOGGER.info("CueBall initial velocity:" + v);
		table.getCueBall().setVelocity(v);
		handleBallsMovements(table);
		table.notify();
	}

	private void handleBallsMovements(PoolTable table) {
		while (true) {
			updatePositionsAndApplyFriction(table);
			detectCollisions(table);
			if (!isAnyBallMoving(table.getRemainingBalls())) {
				break;
			}
			table.notify();
			try {
				table.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void updatePositionsAndApplyFriction(PoolTable table) {
		for (Ball ball : table.getRemainingBalls()) {
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
			// Changes state but method name does not indicate it..
			VectorUtil.calculateAndHandleVectorLength(b.getVelocity());
			// and this return immediately if match is found -> not all balls velocity is calculated/changed.. potential issue but UI-works the same atm.
			if (!b.getVelocity().isZero()) {
				return true;
			}

		}
		return retVal;
	}

	private boolean detectCollisions(PoolTable table) {
		// TODO Auto-generated method stub
		for (int i = 0; i < table.getRemainingBalls().size(); i++) {
			Ball ball = table.getRemainingBalls().get(i);
			// if (ball.isMoving()) {
			checkAndHandleTableCollisions(table, ball);
			// }
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
			// LOGGER.info("No collision between:" + firstBall + " and " + secondBall);
			return;
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
		// System.out.println("After collision A:" + firstBall + " B:" + secondBall);
	}

	private void checkAndHandleTableCollisions(PoolTable table, Ball ball) {
		if (isInMiddleArea(table, ball)) {
			return;
		}
		if (checkAndHandleTableBoundriesCollisions(table, ball)) {
			return;
		}
		checkAndHandlePocketPathwayCollisions(table, ball);
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
			System.out.println("isTableTopBondry");
			ball.getVelocity().y = -1 * ball.getVelocity().y;
			return true;
		}

		if (isTableLeftBoundry(left, ball.getPosition(), radius)) {
			System.out.println("left");
			ball.getVelocity().x = -1 * ball.getVelocity().x;
			return true;
		}

		if (isTableBottomBoundry(bottomLeft, bottomRight, ball.getPosition(), radius)) {
			System.out.println("bottom");
			ball.getVelocity().y = -1 * ball.getVelocity().y;
			return true;
		}
		if (isTableRightBoundry(right, ball.getPosition(), radius)) {
			System.out.println("right");
			ball.getVelocity().x = -1 * ball.getVelocity().x;
			return true;
		}

		return false;
	}

	private void checkAndHandlePocketPathwayCollisions(PoolTable table, Ball ball) {
		for (Pocket pocket : table.getPockets()) {
			if (isInMiddleArea(table, ball)) {
				continue;
			}
			if (this.isPathwayBorderCollision(table, pocket.getPathWayRight(), ball)) {
				System.out.println("IS PATHWAY COL1");
				Vector2d reflectionVector = calculateBallVelocityOnPathwayBorderCollision(table, pocket.getPathWayRight(), ball);
				ball.setVelocity(reflectionVector);
				return;
			} else if (this.isPathwayBorderCollision(table, pocket.getPathwayLeft(), ball)) {
				System.out.println("IS PATHWAY COL2");
				Vector2d reflectionVector = calculateBallVelocityOnPathwayBorderCollision(table, pocket.getPathwayLeft(), ball);
				ball.setVelocity(reflectionVector);
				return;
			}
		}
	}

	private Vector2d projectVectorOnVector(Vector2d a, Vector2d b) {
		double scalar = VectorUtil.dotProduct(a, b) / VectorUtil.dotProduct(b, b);
		return new Vector2d(scalar * b.x, scalar * b.y);
	}

	private Double hypot2(Vector2d a, Vector2d b) {
		Vector2d subtracted1 = VectorUtil.subtractVectors(a, b);
		Vector2d subtracted2 = VectorUtil.subtractVectors(a, b);
		return VectorUtil.dotProduct(subtracted1, subtracted2);
	}

	private boolean isPathwayBorderCollision(PoolTable table, PathWay pathway, Ball ball) {
		// https://stackoverflow.com/questions/1073336/circle-line-segment-collision-detection-algorithm -> answer starting "No one seems to consider projection ..."
		Vector2d a = pathway.getTop();
		Vector2d b = pathway.getBottom();
		Vector2d c = ball.getPosition();
		Vector2d ac = VectorUtil.subtractVectors(c, a);
		Vector2d ab = VectorUtil.subtractVectors(b, a);
		Vector2d projectedVector = this.projectVectorOnVector(ac, ab);
		Vector2d d = VectorUtil.addVectors(projectedVector, a);
		Vector2d ad = VectorUtil.subtractVectors(projectedVector, a);

		double k = Math.abs(ab.x) > Math.abs(ab.y) ? ad.x / ab.x : ad.y / ab.y;
		Double distance = null;
		if (k <= 0.0) {
			distance = Math.sqrt(this.hypot2(c, a));
		} else if (k > 1.0) {
			distance = Math.sqrt(this.hypot2(c, b));
		}
		if (distance == null) {
			distance = Math.sqrt(this.hypot2(c, d));
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

		Vector2d top = VectorUtil.subtractVectors(b.getPosition(), table.getMiddleAreaStart());
		Vector2d bottom = VectorUtil.subtractVectors(table.getMiddleAreaEnd(), b.getPosition());
		if (top.x > 0 && top.y > 0) {
			return bottom.x > 0 && bottom.y > 0;
		}
		return false;
	}

}
