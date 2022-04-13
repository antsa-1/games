package com.tauhka.pool.eightball.tests;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import com.tauhka.games.core.User;
import com.tauhka.games.core.Vector2d;
import com.tauhka.games.pool.Cue;
import com.tauhka.games.pool.CueBall;
import com.tauhka.games.pool.PoolTable;
import com.tauhka.games.pool.PoolTurn;
import com.tauhka.games.pool.TurnResult;

/**
 * @author antsa-1 from GitHub 13 Apr 2022
 **/

public class Tests {

	@Test
	public void gamePlayTest() {
		User u = new User("test");
		PoolTable p = new PoolTable(u, null, false);
		User u2 = new User("best");
		p.joinTableAsPlayer(u2);
		PoolTurn turn = new PoolTurn();
		CueBall cueBall = new CueBall();
		cueBall.setPosition(new Vector2d(268d, 286d));
		PoolTurn poolTurn = p.updateHandBall(u, cueBall);
		assertSame(TurnResult.CONTINUE_TURN, poolTurn.getTurnResult());
		PoolTurn poolTurn2 = new PoolTurn();

		Cue cue = new Cue();
		cue.setForce(150d);
		cue.setAngle(0.08559890034272577d);
		poolTurn2.setCue(cue);
		PoolTurn playedTurn = (PoolTurn) p.playTurn(u, poolTurn2);
		assertSame(TurnResult.CHANGE_TURN, playedTurn.getTurnResult());
		assertSame(16, p.getRemainingBalls().size());
		
		cue.setForce(50d);
		cue.setAngle(-0.4011574892644093d);		
		playedTurn = (PoolTurn) p.playTurn(u2, poolTurn2);
		assertSame(TurnResult.CHANGE_TURN, playedTurn.getTurnResult());
		assertSame(16, p.getRemainingBalls().size());
		cue.setForce(70d);
		cue.setAngle(-2.6109831892758124d);
		playedTurn = (PoolTurn) p.playTurn(u, poolTurn2);
		
	}
}
