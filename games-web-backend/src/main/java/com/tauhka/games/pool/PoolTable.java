package com.tauhka.games.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.User;
import com.tauhka.games.core.Vector2d;
import com.tauhka.games.core.tables.Table;
import com.tauhka.games.core.twodimen.GameResult;
import com.tauhka.games.pool.Ball.Color;
import com.tauhka.games.pool.debug.ServerGUI;

/**
 * @author antsa-1 from GitHub 25 Mar 2022
 **/

public class PoolTable extends Table implements PoolComponent {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(PoolTable.class.getName());
	private Canvas canvas;
	private List<Ball> remainingBalls;
	// LinkedList<Ball> linked = new LinkedList<Ball>();
	private CueBall cueBall;
	private PoolTurn lastTurn;

	public PoolTable(User playerA, GameMode gameMode, boolean randomizeStarter) {
		super(playerA, gameMode, randomizeStarter);
		this.canvas = new Canvas(new Vector2d(1200d, 677d));
		remainingBalls = new ArrayList<Ball>(16);
		Double ballDiameter = 34d;
		for (int i = 0; i < 16; i++) {
			if (i == 0) {
				continue;
			}
			Ball ball = null;
			if (i == 8) {
				Vector2d position = new Vector2d(canvas.getSize().x * 0.65 + (ballDiameter * 0.9d * calculateRackColumn(i)), canvas.getSize().y / 2 + (ballDiameter * 0.5 * this.calculateRackRow(i)));
				ball = new Ball(i, Color.BLACK, position, ballDiameter);
			} else {
				Vector2d position = new Vector2d(canvas.getSize().x * 0.65 + (ballDiameter * 0.9d * calculateRackColumn(i)), canvas.getSize().y / 2 + (ballDiameter * 0.5 * this.calculateRackRow(i)));
				ball = new Ball(i, i % 2 == 0 ? Color.RED : Color.BLACK, position, ballDiameter);
			}
			ball.setVelocity(new Vector2d(0d, 0d));
			this.remainingBalls.add(ball);
		}
		cueBall = new CueBall(0, Color.WHITE, new Vector2d(250d, 311d), ballDiameter);
		cueBall.setVelocity(new Vector2d(0d, 0d));
		this.remainingBalls.add(cueBall);

	}

	@Override
	public synchronized void joinTableAsPlayer(User playerB) {
		super.joinTableAsPlayer(playerB);
		if (isServerGUIWanted()) {
			new ServerGUI(this).start();
			;
		}
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

	public double calculateRackColumn(int i) {
		if (i == 1) {
			return 1;
		} else if (i == 10 || i == 3) {
			return 2;
		} else if (i == 4 || i == 8 || i == 14) {
			return 3;
		} else if (i == 7 || i == 9 || i == 2 || i == 15) {
			return 4;
		}
		return 5;
	}

	public double calculateRackRow(int i) {
		if (i == 1 || i == 8 || i == 5) {
			return 0;
		} else if (i == 5 || i == 3 || i == 15) {
			return 1;
		} else if (i == 10 || i == 2 || i == 15) {
			return -1;
		} else if (i == 4 || i == 12) {
			return -2;
		} else if (i == 6 || i == 14) {
			return 2;
		} else if (i == 7) {
			return 3;
		} else if (i == 9) {
			return -3;
		} else if (i == 13) {
			return -4;
		} else if (i == 11) {
			return 4;
		}
		return -4;
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

	public Vector2d getPosition() {
		return new Vector2d(0d, 0d);
	}

	@Override
	public int getNumber() {
		// TODO Auto-generated method stub
		return -1;
	}

}
