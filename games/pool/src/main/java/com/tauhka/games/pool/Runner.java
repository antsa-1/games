package com.tauhka.games.pool;

import com.tauhka.games.core.User;
import com.tauhka.games.core.Vector2d;
import com.tauhka.games.pool.debug.ServerGUI;

/**
 * @author antsa-1 from GitHub 28 Mar 2022
 **/

public class Runner {

	public static void main(String[] args) throws InterruptedException {
		User u = new User("anonym");
		PoolTable table = new PoolTable(u, null, false, false, 0);
		User u2 = new User("olav");
		table.joinTableAsPlayer(u2);
		CueBall cueBall = initCueBall(261d, 352d);
		table.updateHandBall(u, cueBall);
		PoolTurn turn = new PoolTurn();
		turn.setCue(initCue(250d, -0.013960698528745145d));
		table.playTurn(u, turn);
		turn = new PoolTurn();
		turn.setCue(initCue(250d, -2.1070766760375603d));
		table.playTurn(u, turn);
		new ServerGUI(table).updateSwingComponentPositions();
		table.updateHandBall(u2, initCueBall(401d, 301d));
		turn = new PoolTurn();
		turn.setCue(initCue(225d, 0.44909863562584823d));
		table.playTurn(u2, turn);
		new ServerGUI(table).updateSwingComponentPositions();

		table.updateHandBall(u, initCueBall(537d, 231d));
		turn = new PoolTurn();
		turn.setCue(initCue(250d, -1.4909663410826592d));
		table.playTurn(u, turn);
		new ServerGUI(table).updateSwingComponentPositions();
		table.updateHandBall(u2, initCueBall(401d, 301d));
		turn = new PoolTurn();
		turn.setCue(initCue(225d, 0.44909863562584823d));
		table.playTurn(u2, turn);
		new ServerGUI(table).updateSwingComponentPositions();
	}

	public static CueBall initCueBall(double x, double y) {
		CueBall c = new CueBall();
		Vector2d position = new Vector2d();
		position.x = x;
		position.y = y;
		c.setPosition(position);
		return c;
	}

	public static Cue initCue(double force, double angle) {
		Cue c = new Cue();
		c.setAngle(angle);
		c.setForce(force);
		return c;
	}

}
