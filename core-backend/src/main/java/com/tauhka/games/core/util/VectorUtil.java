package com.tauhka.games.core.util;

import com.tauhka.games.core.Vector2d;

/**
 * @author antsa-1 from GitHub 27 Mar 2022
 */

public class VectorUtil {

	public static double dotProduct(Vector2d vectorA, Vector2d vectorB) {

		return vectorA.x * vectorB.x + vectorA.y * vectorB.y;
	}

	public static Vector2d subtractVectors(Vector2d vectorA, Vector2d vectorB) {

		return new Vector2d(vectorA.x - vectorB.x, vectorA.y - vectorB.y);
	}

	public static Vector2d addVectors(Vector2d vectorA, Vector2d vectorB) {

		return new Vector2d(vectorA.x + vectorB.x, vectorA.y + vectorB.y);
	}

	public static Vector2d copy(Vector2d v) {
		return new Vector2d(v.x, v.y);
	}

	public static Vector2d multiply(Vector2d vector, Double scalar) {
		return new Vector2d(vector.x * scalar, vector.y * scalar);
	}

	public static double calculateAndHandleVectorLength(Vector2d vector) {
		double res = VectorUtil.calculateVectorLength(vector);
		if (res < 5) {
			vector.x = 0;
			vector.y = 0;
		}
		return res;
	}

	public static double calculateVectorLength(Vector2d vector) {
		double dx = Math.pow(vector.x, 2);
		double dy = Math.pow(vector.y, 2);
		return Math.sqrt(dx + dy);
	}

	public static Vector2d calculateCueBallInitialVelocity(double initialForce, double angle) {
		double x = initialForce * Math.cos(angle);
		double y = initialForce * Math.sin(angle);
		return new Vector2d(x, y);
	}

	public static boolean isZero(Vector2d v) {
		return v.x == 0d && v.y == 0d;
	}

	public static Vector2d projectVectorOnVector(Vector2d a, Vector2d b) {
		double scalar = VectorUtil.dotProduct(a, b) / VectorUtil.dotProduct(b, b);
		return new Vector2d(scalar * b.x, scalar * b.y);
	}

	public static Double hypot2(Vector2d a, Vector2d b) {
		Vector2d subtracted1 = VectorUtil.subtractVectors(a, b);
		Vector2d subtracted2 = VectorUtil.subtractVectors(a, b);
		return VectorUtil.dotProduct(subtracted1, subtracted2);
	}
}
