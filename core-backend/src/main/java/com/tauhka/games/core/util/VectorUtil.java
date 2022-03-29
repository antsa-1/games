package com.tauhka.games.core.util;

import com.tauhka.games.core.Vector2d;

/**
 * @author antsa-1 from GitHub 27 Mar 2022
 */

public class VectorUtil {

	public static Double dotProduct(Vector2d vectorA, Vector2d vectorB) {

		return vectorA.x * vectorB.x + vectorA.y * vectorB.y;
	}

	public static Vector2d subtractVectors(Vector2d vectorA, Vector2d vectorB) {

		return new Vector2d(vectorA.x - vectorB.x, vectorA.y - vectorB.y);
	}

	public static Vector2d addVectors(Vector2d vectorA, Vector2d vectorB) {

		return new Vector2d(vectorA.x + vectorB.x, vectorA.y + vectorB.y);
	}

	public static void multiplyVector(Vector2d vector, Double scalar) {
		vector.x = vector.x * scalar;
		vector.y = vector.y * scalar;
	}

	public static double calculateVectorLength(Vector2d vector) {
		double dx = (Math.pow(vector.x, 2));
		double dy = (Math.pow(vector.y, 2));
		double res = Math.sqrt(dx + dy);
		if (res < 5) {
			return 0;
		}
		return res;
	}

	public static Vector2d calculateCueBallInitialVelocity(double initialForce, double angle) {
		double x = initialForce * Math.cos(angle);
		double y = initialForce * Math.sin(angle);
		return new Vector2d(x, y);
	}

	public static boolean isZero(Vector2d v) {
		return v.x == 0 && v.y == 0;
	}
}
