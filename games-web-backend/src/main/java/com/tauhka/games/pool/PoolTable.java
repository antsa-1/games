package com.tauhka.games.pool;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.Table;
import com.tauhka.games.core.User;

/**
 * @author antsa-1 from GitHub 6 Mar 2022
 **/

public class PoolTable extends Table {

	public PoolTable(User playerA, GameMode gameMode, boolean randomizeStarter) {
		super(playerA, gameMode, randomizeStarter);
	}

	private int xPosition;
	private int yPosition;
}
