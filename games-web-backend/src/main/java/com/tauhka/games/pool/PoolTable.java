package com.tauhka.games.pool;

import java.util.List;
import java.util.logging.Logger;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.User;
import com.tauhka.games.core.Vector2d;
import com.tauhka.games.core.tables.Table;
import com.tauhka.games.core.twodimen.GameResult;
import com.tauhka.games.pool.debug.ServerGUI;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;

/**
 * @author antsa-1 from GitHub 25 Mar 2022
 **/

public class PoolTable extends Table implements PoolComponent {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(PoolTable.class.getName());

	@JsonbProperty("turn")
	private PoolTurn turn;

	@JsonbProperty("remainingBalls")
	private List<Ball> remainingBalls;
	@JsonbProperty("playerABalls")
	private List<Ball> playerABalls; // Player properties
	@JsonbProperty("playerBBalls")
	private List<Ball> playerBBalls; // Player properties
	@JsonbTransient
	private List<Pocket> pockets;
	@JsonbTransient
	private List<Boundry> boundries;
	@JsonbTransient
	private Vector2d middleAreaStart;
	@JsonbTransient
	private Vector2d middleAreaEnd;
	@JsonbTransient
	private CueBall cueBall;
	@JsonbTransient
	private Canvas canvas;
	@JsonbTransient
	private EightBallRuleBase eightBallRuleBase;
	@JsonbTransient
	private static final boolean SERVER_GUI;
	@JsonbTransient
	private boolean expectingHandBallUpdate;
	static {
		String env = System.getProperty("Server_Environment");
		String ui = System.getProperty("Server_ShowUI");
		SERVER_GUI = true;
		/* else if (env.equalsIgnoreCase(Constants.ENVIRONMENT_DEVELOPMENT) && gui.equalsIgnoreCase("Server_ActivateGUI")) { SERVER_GUI = true; } else { SERVER_GUI = true; } */
	}

	public PoolTable(User playerA, GameMode gameMode, boolean randomizeStarter) {
		// PoolPlayer p = new PoolPlayer() super(p,gameMode,randomizeStarte)
		super(playerA, gameMode, randomizeStarter);
		PoolTableInitializer.init(this);
	}

	@Override
	public Object playTurn(User user, Object o) {
		if (!user.equals(this.playerInTurn)) {
			throw new IllegalArgumentException("Player is not in turn in table:" + this);
		}
		if (expectingHandBallUpdate) {
			throw new IllegalArgumentException("Expecting handball update:" + this);
		}
		PoolTurn turn = (PoolTurn) o;

		// VectorUtil.copy(middleAreaEnd);
		TurnResult turnResult = null;
		if (SERVER_GUI) {
			LOGGER.info("Pooltable instance, server in testing mode");
			synchronized (this) {
				// When second pool turn comes from browser this blocks and does not proceed .. TODO
				turnResult = this.eightBallRuleBase.playTurn(this, turn);
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
			turnResult = this.eightBallRuleBase.playTurn(this, turn);
		}
		if (turnResult == TurnResult.CHANGE_TURN) {
			changePlayerInTurn();
		}
		if (turnResult == TurnResult.HANDBALL) {
			changePlayerInTurn();
			expectingHandBallUpdate = true;
		}
		PoolTurn playedTurn = new PoolTurn();
		playedTurn.setTurnResult(turnResult.toString());
		playedTurn.setCue(turn.getCue());
		return playedTurn;

	}

	@Override
	public synchronized void joinTableAsPlayer(User playerB) {
		super.joinTableAsPlayer(playerB);
		if (SERVER_GUI) {
			new Thread(new ServerGUI(this)).start();
		}
	}

	public boolean updateHandBall(User user, CueBall sample) {
		if (!user.equals(this.playerInTurn)) {
			throw new IllegalArgumentException("Player is not in turn in table:" + this);
		}
		if (!expectingHandBallUpdate) {
			throw new IllegalArgumentException("HandBall update is not expected:" + this);
		}
		if (handBallPositionAllowed(sample)) {
			// Different canvas sizes .. TODO
			Vector2d position = sample.getPosition();
			this.cueBall.setPosition(position);
			synchronized (this) {
				this.notify();
				try {
					LOGGER.info("Pooltable instance waits handball gui repaint");
					this.wait();
					LOGGER.info("Pooltable instance waits handball continues");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			expectingHandBallUpdate = false;
			return true;
		}
		System.out.println("Handball position not allowed" + sample);
		return false;
	}

	private boolean handBallPositionAllowed(CueBall cueBall2) {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isOpen() {
		// Open table -> no balls inside pocket yet
		return this.getRemainingBalls().size() == 16;
	}

	@JsonbTransient
	public List<Ball> getPlayerInTurnBalls() {
		if (playerInTurn.equals(playerA)) {
			return this.playerABalls;
		}
		return this.playerBBalls;
	}

	@JsonbTransient
	public List<Ball> getPlayerNotInTurnBalls() {
		if (playerInTurn.equals(playerA)) {
			return this.playerBBalls;
		}
		return this.playerABalls;
	}

	public void handleHandBall(CueBall cueBall, Vector2d newPosition) {
		if (eightBallRuleBase.isCueBallNewPositionAllowed(this, newPosition)) {
			this.cueBall.setPosition(newPosition);
		}
		throw new IllegalArgumentException("CueBall new position is not allowed:" + newPosition);
	}

	public void putBallInPocket(Ball ballToPocket, Pocket pocket) {
		if (ballToPocket.getNumber() == 0) {
			return;
		}
		ballToPocket.setInPocket(true);
		Ball ballInPocket = this.playerABalls.isEmpty() ? null : this.playerABalls.get(0);
		if (ballInPocket != null) {
			if (ballInPocket.isSimilar(ballToPocket)) {
				this.playerABalls.add(ballToPocket);
				return;
			}
			this.playerBBalls.add(ballToPocket);
			return;
		}
		ballInPocket = this.playerBBalls.isEmpty() ? null : this.playerBBalls.get(0);
		if (ballInPocket != null) {
			if (ballInPocket.isSimilar(ballToPocket)) {
				this.playerBBalls.add(ballToPocket);
				return;
			}
			this.playerABalls.add(ballToPocket);
			return;
		}
		if (playerA.equals(playerInTurn)) {
			this.playerABalls.add(ballToPocket);
			return;
		}
		this.playerBBalls.add(ballToPocket);
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

	public List<Ball> getPlayerABalls() {
		return playerABalls;
	}

	public void setPlayerABalls(List<Ball> playerABalls) {
		this.playerABalls = playerABalls;
	}

	public List<Ball> getPlayerBBalls() {
		return playerBBalls;
	}

	public void setPlayerBBalls(List<Ball> playerBBalls) {
		this.playerBBalls = playerBBalls;
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

	public EightBallRuleBase getEightBallRuleBase() {
		return eightBallRuleBase;
	}

	public void setEightBallRuleBase(EightBallRuleBase eightBallRuleBase) {
		this.eightBallRuleBase = eightBallRuleBase;
	}

}
