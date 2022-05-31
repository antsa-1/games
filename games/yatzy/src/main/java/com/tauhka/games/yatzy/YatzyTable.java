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
	private static final long serialVersionUID = 1L;
	private Timer timer;
	private YatzyRuleBase yatzyRuleBase;

	public YatzyTable(User playerA, GameMode gameMode, boolean randomizeStarter, boolean registeredOnly, int timeControlIndex, int playerAmount) {
		super(playerA, gameMode, randomizeStarter, registeredOnly, timeControlIndex, playerAmount);
	}

	public YatzyTable() {
		// Empty constr for deserialization
		super(null, null, false, false, 0, 0);
	}

	@Override
	public GameResult checkWinAndDraw() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Dice> throwDices(User user, Object arg1) {
		return yatzyRuleBase.throwUnlockedDices((YatzyPlayer) this.getPlayerInTurn());
	}

	public void lockDices(User user, Object arg1) {
		yatzyRuleBase.lockDices((YatzyPlayer) this.getPlayerInTurn(), null);
	}

	public void selectHand(User user, Object arg1) {
		yatzyRuleBase.selectHand((YatzyPlayer) this.getPlayerInTurn(), null);
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

}
