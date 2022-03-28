package com.tauhka.games.core.util;

import com.tauhka.games.core.Vector2d;

/**
 * @author antsa-1 from GitHub 27 Mar 2022
 */

/*
* hypot2(vectorA:IVector2, vectorB:IVector2){
			return this.dotProduct(this.subtractVectors(vectorA, vectorB), this.subtractVectors(vectorA, vectorB))
		},
		
		projectVectorOnVector(vectorA:IVector2, vectorB:IVector2){
			const scalar = this.dotProduct(vectorA, vectorB) / this.dotProduct(vectorB, vectorB)
			return <IVector2>{x: scalar * vectorB.x, y: scalar * vectorB.y}
		},			
		calculateLength(vector:IVector2){
			// = same as magnitude of Vector
			const length = Math.sqrt(Math.pow(vector.x, 2) + Math.pow(vector.y, 2))			
			return length
		},
		calculateVectorLength(component:IPoolComponent) :IPoolComponent {
			//Magnitude
			const length = Math.sqrt((Math.pow(component.velocity.x, 2) + Math.pow(component.velocity.y, 2)))			
			if(length < 5){				
				component.velocity = <IVector2> {x: 0, y: 0}
			}
			return component
		},
		multiplyVector(vector:IVector2, scalar:number ): IVector2{
			return <IVector2>{ x: vector.x * scalar, y: vector.y * scalar}
		},
		addVectors(vectorA:IVector2, vectorB: IVector2 ): IVector2{
			return <IVector2>{ x: vectorA.x + vectorB.x, y: vectorA.y + vectorB.y}
		},
		subtractVectors(vectorA:IVector2, vectorB: IVector2 ): IVector2{
			return <IVector2>{ x: vectorA.x - vectorB.x, y: vectorA.y - vectorB.y}
		},
		dotProduct(vectorA:IVector2, vectorB: IVector2 ): number{
			return vectorA.x * vectorB.x + vectorA.y * vectorB.y
		},
* 
*/
public class VectorUtil {

	public static Double dotProduct(Vector2d vectorA, Vector2d vectorB) {

		return vectorA.getX() * vectorB.getX() + vectorA.getY() * vectorB.getY();
	}

	public static Vector2d subtractVectors(Vector2d vectorA, Vector2d vectorB) {

		return new Vector2d(vectorA.getX() - vectorB.getX(), vectorA.getY() - vectorB.getY());
	}

	public static Vector2d addVectors(Vector2d vectorA, Vector2d vectorB) {

		return new Vector2d(vectorA.getX() + vectorB.getX(), vectorA.getY() + vectorB.getY());
	}

	public static Vector2d multiplyVector(Vector2d vector, Double scalar) {

		return new Vector2d(vector.getX() * scalar, vector.getY() * scalar);
	}

	public static Vector2d calculateVectorLength(Vector2d vector) {
		vector.setX(Math.pow(vector.getX(), 2));
		vector.setY(Math.pow(vector.getY(), 2));
		Double res = Math.sqrt(vector.getX() + vector.getY());
		if (res < 5) {
			return new Vector2d(0d, 0d);
		}
		return vector;
	}
}
