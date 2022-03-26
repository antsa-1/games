package com.tauhka.games.core.tables;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.Move;
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
	public synchronized Move playTurn(User user, Integer i1, Integer i2) {
		// TODO Auto-generated method stub
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
