package com.tauhka.games.pool.debug;

import com.tauhka.games.core.User;
import com.tauhka.games.pool.Cue;
import com.tauhka.games.pool.PoolTable;
import com.tauhka.games.pool.PoolTurn;

/**
 * @author antsa-1 from GitHub 28 Mar 2022
 **/

public class Runner {

	public static void main(String[] args) throws InterruptedException {
		User u = new User("test");
		PoolTable p = new PoolTable(u, null, false);

		User u2 = new User("best");
		p.joinTableAsPlayer(u2);
		PoolTurn turn = new PoolTurn();
		Cue cue = new Cue();
		// -0.01425565337077744 -> osuu force 70
		// -0.18982631837040745d) -> yli force 65
		// 0.04085316949817506  force 200 two bottom pocket pathway collisions
		cue.setAngle(0.04085316949817506d);
		cue.setForce(200d);
		turn.setCue(cue);
		p.playTurn(u, turn);
		

	}

}
