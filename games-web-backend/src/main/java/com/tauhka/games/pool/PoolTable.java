package com.tauhka.games.pool;

import java.util.List;
import java.util.logging.Logger;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.User;
import com.tauhka.games.core.Vector2d;
import com.tauhka.games.core.tables.Table;
import com.tauhka.games.core.twodimen.GameResult;
import com.tauhka.games.pool.debug.ServerGUI;

/**
 * @author antsa-1 from GitHub 25 Mar 2022
 **/

public class PoolTable extends Table implements PoolComponent {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(PoolTable.class.getName());
	private Canvas canvas;
	private List<Ball> remainingBalls;
	private List<Pocket> pockets;
	private List<Boundry> boundries;
	private Vector2d middleAreaStart;
	private Vector2d middleAreaEnd;
	private CueBall cueBall;
	private PoolTurn lastTurn;

	public PoolTable(User playerA, GameMode gameMode, boolean randomizeStarter) {
		super(playerA, gameMode, randomizeStarter);
		PoolTableInitializer.init(this);
		System.out.println("sds");
	}

	@Override
	public synchronized void joinTableAsPlayer(User playerB) {
		super.joinTableAsPlayer(playerB);
		if (isServerGUIWanted()) {
			new Thread(new ServerGUI(this)).start();
		}
	}

	public boolean isInPocket(Ball ball) {
		return false;
	}

	private boolean isServerGUIWanted() {
		// System.getProperty("debugMode");
		return true;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	public void setCanvas(Canvas canvas) {
		this.canvas = canvas;
	}

	public CueBall getCueBall() {
		return cueBall;
	}

	public void setCueBall(CueBall cueBall) {
		this.cueBall = cueBall;
	}

	public List<Boundry> getBoundries() {
		return boundries;
	}

	public void setBoundries(List<Boundry> boundries) {
		this.boundries = boundries;
	}

	public List<Pocket> getPockets() {
		return pockets;
	}

	public void setPockets(List<Pocket> pockets) {
		this.pockets = pockets;
	}

	@Override
	public Object playTurn(User user, Object o) {
		if (!user.equals(this.playerInTurn)) {
			throw new IllegalArgumentException("Player is not in turn in table:" + this);
		}
		PoolTurn turn = (PoolTurn) o;
		if (isServerGUIWanted()) {
			LOGGER.info("Pooltable instance, server in testing mode");
			synchronized (this) {
				new EightBallRuleBase().handleMovements(this, turn);
				LOGGER.info("PoolTable instance made movements now notifying, threadId:" + Thread.currentThread().getId());
				this.notify();
				try {
					LOGGER.info("Pooltable instance waits for Swing components to update");
					this.wait();
					LOGGER.info("pooltable waiting done, continues");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					LOGGER.info("Pooltable instance, thread interrupted" + Thread.currentThread().getId());
				}
			}
		} else {
			new EightBallRuleBase().handleMovements(this, turn);
		}
		LOGGER.info("pooltable instance, turn played, changing turn");
		changePlayerInTurn();
		return null;
	}

	public List<Ball> getRemainingBalls() {
		return remainingBalls;
	}

	public void setRemainingBalls(List<Ball> remainingBalls) {
		this.remainingBalls = remainingBalls;
	}

	@Override
	public GameResult checkWinAndDraw() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Table startRematch() {
		return this;
	}

	@Override
	public synchronized boolean suggestRematch(User user) {

		super.suggestRematch(this.playerA);
		super.suggestRematch(this.playerB);
		this.notify();
		return true;
	}

	public Vector2d getPosition() {
		return new Vector2d(0d, 0d);
	}

	@Override
	public int getNumber() {
		// TODO Auto-generated method stub
		return -1;
	}

	public Vector2d getMiddleAreaStart() {
		return middleAreaStart;
	}

	public void setMiddleAreaStart(Vector2d middleAreaStart) {
		this.middleAreaStart = middleAreaStart;
	}

	public Vector2d getMiddleAreaEnd() {
		return middleAreaEnd;
	}

	public void setMiddleAreaEnd(Vector2d middleAreaEnd) {
		this.middleAreaEnd = middleAreaEnd;
	}

}
