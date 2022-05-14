package com.tauhka.games.yatzy;

import java.util.List;
import java.util.Timer;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.User;
import com.tauhka.games.core.tables.MultiplayerTable;
import com.tauhka.games.core.tables.Table;
import com.tauhka.games.core.twodimen.GameResult;

/**
 * @author antsa-1 from GitHub 12 May 2022
 **/

public class YatzyTable extends MultiplayerTable {
	private Timer timer;
	private YatzyGame yatzyGame;

	public YatzyTable(User playerA, GameMode gameMode, boolean randomizeStarter, boolean registeredOnly, int timeControlIndex, int playerAmount) {
		super(playerA, gameMode, randomizeStarter, registeredOnly, timeControlIndex, playerAmount);
		// TODO Auto-generated constructor stub
	}

	@Override
	public GameResult checkWinAndDraw() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object playTurn(User arg0, Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object lockDices(List<Integer> diceIds) {
		return null;
	}

	@Override
	protected Table startRematch() {
		// TODO Auto-generated method stub
		return null;
	}

	public synchronized boolean joinTableAsPlayer(User player) {
		if (super.joinTableAsPlayer(player)) {
			// Start game
			// return true
		}
		return false;
	}
}
