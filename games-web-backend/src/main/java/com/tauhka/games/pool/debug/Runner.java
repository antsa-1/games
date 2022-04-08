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
		// 0.04085316949817506 force 200 two bottom pocket pathway collisions
		// 0.0522503382826774d / 220d -> top left collision to table

		cue.setAngle(0.0574905344833121d); 
		cue.setForce(250d);
		turn.setCue(cue);
		p.playTurn(u,turn);
		/* SET1 cue.setAngle(0.052751015925788396d); cue.setForce(120d); turn.setCue(cue); p.playTurn(u, turn); Thread.sleep(1000);
		 * 
		 * cue.setAngle(0.23825619040923732d); cue.setForce(60d); turn.setCue(cue); p.playTurn(u2, turn); Thread.sleep(1000);
		 * 
		 * cue.setAngle(-1.7034643736887434d); cue.setForce(60d); turn.setCue(cue); p.playTurn(u2, turn); Thread.sleep(1000); */
		
		/*
		 * SET2
		 
		cue.setAngle(0.034159369237127396d);
		cue.setForce(160d);
		turn.setCue(cue);
		p.playTurn(u, turn);
		Thread.sleep(2000);

		cue.setAngle(0.9834061553082604d);
		cue.setForce(60d);
		turn.setCue(cue);
		p.playTurn(u2, turn);
		Thread.sleep(2000);

		cue.setAngle(-3.1286069397220464d);
		cue.setForce(160d);
		turn.setCue(cue);
		p.playTurn(u, turn);
		Thread.sleep(2000);
		*/

//JSON for cue {"angle":0.0522503382826774,"force":220,"position":{"x":250,"y":311,"zero":false}}
	}

}
