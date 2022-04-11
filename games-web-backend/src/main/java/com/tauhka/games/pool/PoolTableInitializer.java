package com.tauhka.games.pool;

import java.util.ArrayList;
import java.util.List;

import com.tauhka.games.core.Vector2d;
import com.tauhka.games.pool.Ball.Color;

/**
 * @author antsa-1 from GitHub 31 Mar 2022
 **/

public class PoolTableInitializer {

	public static void init(PoolTable table) {
		Canvas canvas = new Canvas(new Vector2d(1200d, 677d));
		List<Ball> remainingBalls = new ArrayList<Ball>(16);
		Double ballDiameter = 34d;
		for (int i = 0; i < 16; i++) {
			if (i == 0) {
				continue;
			}
			Ball ball = null;
			if (i == 8) {
				Vector2d position = new Vector2d(canvas.getSize().x * 0.65 + (ballDiameter * 0.9d * calculateRackColumn(i)), canvas.getSize().y / 2 + (ballDiameter * 0.5 * PoolTableInitializer.calculateRackRow(i)));
				ball = new Ball(i, Color.BLACK, position, ballDiameter);
			} else {
				Vector2d position = new Vector2d(canvas.getSize().x * 0.65 + (ballDiameter * 0.9d * calculateRackColumn(i)), canvas.getSize().y / 2 + (ballDiameter * 0.5 * PoolTableInitializer.calculateRackRow(i)));
				ball = new Ball(i, i < 8 ? Color.RED : Color.YELLOW, position, ballDiameter);
			}
			ball.setVelocity(new Vector2d(0d, 0d));
			remainingBalls.add(ball);
		}
		CueBall cueBall = new CueBall(0, Color.WHITE, new Vector2d(250d, 311d), ballDiameter);
		cueBall.setVelocity(new Vector2d(0d, 0d));
		remainingBalls.add(cueBall);
		List<Boundry> boundries = PoolTableInitializer.initBoundries();
		table.setCueBall(cueBall);
		table.setRemainingBalls(remainingBalls);
		table.setMiddleAreaStart(new Vector2d(125d, 130d));
		table.setMiddleAreaEnd(new Vector2d(1072d, 550d));
		table.setBoundries(boundries);
		table.setCanvas(canvas);
		table.setPockets(PoolTableInitializer.initPockets());
		table.setPlayerABalls(new ArrayList<Ball>(8));
		table.setPlayerBBalls(new ArrayList<Ball>(8));
		table.setEightBallRuleBase(new EightBallRuleBase());
	}

	private static List<Pocket> initPockets() {
		List<Pocket> pockets = new ArrayList<Pocket>(6);
		double radiusBig = 32d;
		double radiusSmall = 28d;
		PathWay left = new PathWay(new Vector2d(54d, 93d), new Vector2d(80d, 118d));
		PathWay right = new PathWay(new Vector2d(96d, 56d), new Vector2d(117d, 77d));
		Vector2d center = new Vector2d(65d, 61d);
		Pocket pocket = new Pocket(center, radiusBig, left, right);
		pockets.add(pocket);

		left = new PathWay(new Vector2d(580d, 56d), new Vector2d(571d, 79d));
		right = new PathWay(new Vector2d(635d, 56d), new Vector2d(644d, 79d));
		center = new Vector2d(607d, 49d);
		pocket = new Pocket(center, radiusSmall, left, right);
		pockets.add(pocket);

		left = new PathWay(new Vector2d(1112d, 56d), new Vector2d(1089d, 79d));
		right = new PathWay(new Vector2d(1144d, 93d), new Vector2d(1122d, 118d));
		center = new Vector2d(1144d, 61d);
		pocket = new Pocket(center, radiusBig, left, right);
		pockets.add(pocket);

		left = new PathWay(new Vector2d(1091d, 600d), new Vector2d(1112d, 620d));
		right = new PathWay(new Vector2d(1122d, 562d), new Vector2d(1142d, 580d));
		center = new Vector2d(1144d, 614d);
		pocket = new Pocket(center, radiusBig, left, right);
		pockets.add(pocket);

		left = new PathWay(new Vector2d(571d, 600d), new Vector2d(580d, 620d));
		right = new PathWay(new Vector2d(644d, 600d), new Vector2d(635d, 620d));
		center = new Vector2d(607d, 628d);
		pocket = new Pocket(center, radiusSmall, left, right);
		pockets.add(pocket);

		left = new PathWay(new Vector2d(80d, 558d), new Vector2d(54d, 584d));
		right = new PathWay(new Vector2d(118d, 600d), new Vector2d(98d, 620d));
		center = new Vector2d(65d, 614d);
		pocket = new Pocket(center, radiusBig, left, right);
		pockets.add(pocket);
		return pockets;
	}

	private static List<Boundry> initBoundries() {
		Boundry topLeft = new Boundry(79, 118, 572);
		Boundry topRight = new Boundry(79, 644, 1090);
		Boundry right = new Boundry(1122, 118, 560);
		Boundry bottomRight = new Boundry(600, 644, 1090);
		Boundry bottomLeft = new Boundry(600, 118, 572);
		Boundry left = new Boundry(80, 118, 560);
		List<Boundry> boundries = new ArrayList<Boundry>(6);
		boundries.add(topLeft);
		boundries.add(topRight);
		boundries.add(right);
		boundries.add(bottomRight);
		boundries.add(bottomLeft);
		boundries.add(left);
		return boundries;
	}

	public static double calculateRackColumn(int i) {
		if (i == 1) {
			return 1;
		} else if (i == 10 || i == 3) {
			return 2;
		} else if (i == 4 || i == 8 || i == 14) {
			return 3;
		} else if (i == 7 || i == 9 || i == 2 || i == 15) {
			return 4;
		}
		return 5;
	}

	public static double calculateRackRow(int i) {
		if (i == 1 || i == 8 || i == 13) {
			return 0;
		} else if ( i == 3 || i == 15) {
			return 1;
		} else if (i == 10 || i == 2 || i == 15) {
			return -1;
		} else if (i == 4 || i == 12) {
			return -2;
		} else if (i == 6 || i == 14) {
			return 2;
		} else if (i == 7) {
			return 3;
		} else if (i == 9) {
			return -3;
		} else if (i == 5) {
			return -4;
		} else if (i == 11) {
			return 4;
		}
		return -4;
	}
}
