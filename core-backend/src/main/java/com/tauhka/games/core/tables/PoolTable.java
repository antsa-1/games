package com.tauhka.games.core.tables;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.User;
import com.tauhka.games.core.twodimen.GameResult;

/**
 * @author antsa-1 from GitHub 25 Mar 2022
 **/

public class PoolTable extends Table {

	private static final long serialVersionUID = 1L;

	public PoolTable(User playerA, GameMode gameMode, boolean randomizeStarter) {
		super(playerA, gameMode, randomizeStarter);
		// TODO Auto-generated constructor stub
	}
	

	@Override
	public Object playTurn(User user, Object o) {
		if (!user.equals(this.playerInTurn)) {
			throw new IllegalArgumentException("Player is not in turn in table:" + this);
		}

		changePlayerInTurn();
		return null;
	}

	@Override
	public GameResult checkWinAndDraw() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Table startRematch() {
		// TODO Auto-generated method stub
		return null;
	}

}
