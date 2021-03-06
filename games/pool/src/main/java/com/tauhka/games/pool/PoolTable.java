package com.tauhka.games.pool;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.User;
import com.tauhka.games.core.Vector2d;
import com.tauhka.games.core.tables.Table;
import com.tauhka.games.core.twodimen.GameResult;
import com.tauhka.games.pool.debug.ServerGUI;
import com.tauhka.games.pool.eightball.EightBallInitializer;
import com.tauhka.games.pool.eightball.EightBallRuleBase;

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
	private Pocket selectedPocket; // Player properties
	@JsonbTransient
	private CueBall cueBall;
	@JsonbTransient
	private List<Pocket> pockets;
	@JsonbTransient
	private List<Boundry> boundries;
	@JsonbTransient
	private Vector2d middleAreaStart;
	@JsonbTransient
	private Vector2d middleAreaEnd;
	@JsonbTransient
	private Canvas canvas;
	@JsonbTransient
	private EightBallRuleBase eightBallRuleBase;
	@JsonbTransient
	private static final boolean SERVER_GUI;
	@JsonbTransient
	private boolean expectingHandBallUpdate;
	@JsonbTransient
	private boolean expectingPocketSelection;
	@JsonbTransient
	private TurnResult turnResult;
	@JsonbTransient
	private UUID gameId;
	@JsonbTransient
	private boolean breaked;
	
	static {
		String env = System.getProperty("Server_Environment");
		String ui = System.getProperty("Server_ShowUI");
		SERVER_GUI = false;
		/* else if (env.equalsIgnoreCase(Constants.ENVIRONMENT_DEVELOPMENT) && gui.equalsIgnoreCase("Server_ActivateGUI")) { SERVER_GUI = true; } else { SERVER_GUI = true; } */
	}

	public PoolTable(User playerA, GameMode gameMode, boolean randomizeStarter, boolean registeredOnly, int timeControlIndex) {
		super(playerA, gameMode, randomizeStarter, registeredOnly, timeControlIndex);
		EightBallInitializer.init(this);
		expectingHandBallUpdate = true;
	//	s=new ServerGUI(this);
	}

	@Override
	public synchronized Object playTurn(User user, Object o) {
		LOGGER.info("Playing turn:" + user.getName());
		checkBasicGuards(user);
		if (expectingPocketSelection) {
			throw new IllegalArgumentException("Expecting pocket selection:" + this + " user:" + user.getName());
		}
		if (expectingHandBallUpdate) {
			throw new IllegalArgumentException("Expecting handball update:" + this + " user:" + user.getName());
		}
		PoolTurn turn = (PoolTurn) o;

		TurnResult turnResult = this.eightBallRuleBase.playTurn(this, turn);

		PoolTurn playedTurn = new PoolTurn();
		playedTurn.setPlayer(user.getName());
		if (TurnResult.isDecisive(turnResult)) {
			gameOver = true;
			checkWinner(turnResult, playedTurn);
			super.initRematchForArtificialPlayer();
		}
		if (turnResult == TurnResult.CHANGE_TURN) {
			changePlayerInTurn();
		}
		if (turnResult == TurnResult.HANDBALL) {
			changePlayerInTurn();
			expectingHandBallUpdate = true;
		}
		if (turnResult != TurnResult.HANDBALL && playedTurn.getWinner() == null && getPlayerInTurnBalls().size() == 7) {
			turnResult = TurnResult.ASK_POCKET_SELECTION;
			expectingPocketSelection = true;
		}

		playedTurn.setTurnResult(turnResult);
		playedTurn.setCue(turn.getCue());
		playedTurn.setCueBall(cueBall);
		this.selectedPocket = null;
		LOGGER.info("PLAYED TURN turnResult = " + turnResult.toString() + " player:" + user.getName());
		/*
		for (Ball b : remainingBalls) {
			System.out.println("B:" + b.getNumber() + " pos:" + b.getPosition() + " veloc:" + b.getVelocity());
		s.updateSwingComponentPositions();
		}
		*/
		
		return playedTurn;
	}

	private void checkBasicGuards(User user) {
		if (gameOver) {
			LOGGER.info("GameOver in table " + this + " user:" + user.getName());
			throw new IllegalArgumentException("GameOver in table:" + this + " user:" + user.getName());
		}
		if (!user.equals(this.playerInTurn)) {
			throw new IllegalArgumentException("Player is not in turn in table:" + this + " user:" + user.getName());
		}
	}

	private void checkWinner(TurnResult turnResult, PoolTurn playedTurn) {
		if (TurnResult.EIGHT_BALL_IN_POCKET_OK == turnResult) {
			playedTurn.setWinner(getPlayerInTurn());
		} else if (TurnResult.EIGHT_BALL_IN_POCKET_FAIL == turnResult) {
			if (playerInTurn.equals(playerA)) {
				playedTurn.setWinner(playerB);
			} else {
				playedTurn.setWinner(playerA);
			}
		}
	}

	@Override
	public synchronized void joinTableAsPlayer(User playerB) {
		super.joinTableAsPlayer(playerB);

	}

	public void setBreaked(boolean breaked) {
		this.breaked = breaked;
	}

	public boolean isBreaked() {
		return breaked;
	}

	public synchronized PoolTurn updateHandBall(User user, CueBall sample) {
		LOGGER.info("Updatehandball by:" + user.getName());
		checkBasicGuards(user);
		if (expectingPocketSelection) {
			throw new IllegalArgumentException("HandBall update is not expected:" + this);
		}
		if (!expectingHandBallUpdate) {
			throw new IllegalArgumentException("HandBall update is not expected:" + this);
		}
		PoolTurn turn = new PoolTurn();
		turn.setPlayer(user.getName());
		if (eightBallRuleBase.isCueBallNewPositionAllowed(this, sample)) {
			// Different canvas sizes .. TODO
			Vector2d position = sample.getPosition();
			this.cueBall.setPosition(position);
			turn.setCueBall(this.cueBall);
			this.cueBall.setInPocket(false);
			expectingHandBallUpdate = false;
			if (getPlayerInTurnBalls().size() == 7) {
				this.expectingPocketSelection = true;
				turn.setTurnResult(TurnResult.ASK_POCKET_SELECTION);
				turn.setCueBall(sample);
				LOGGER.fine("Updatehandball updated by:" + user.getName());
				return turn;
			}
			LOGGER.info("Handball position was allowed" + sample);
			turn.setTurnResult(TurnResult.CONTINUE_TURN);
			turn.setCueBall(sample);
			LOGGER.fine("Updatehandball updated by:" + user.getName());
			return turn;
		} else {
			LOGGER.info("Handball position not allowed" + sample + " user:" + user);
			turn.setTurnResult(TurnResult.HANDBALL_FAIL);
			turn.setCueBall(sample);
			LOGGER.fine("Updatehandball updated by:" + user.getName());
			return turn;
		}
	}

	public synchronized PoolTurn selectPocket(User user, Integer pocketNumber) {
		LOGGER.fine("selectPocket  by:" + user.getName());
		checkBasicGuards(user);
		if (!expectingPocketSelection) {
			throw new IllegalArgumentException("It's not allowed to select pocket update is not expected:" + user + " _ " + this);
		}
		if (pocketNumber == null || !(pocketNumber >= 0) || !(pocketNumber <= 6)) {
			throw new IllegalArgumentException("No such pocket" + user + " _ " + pocketNumber);
		}
		this.selectedPocket = this.pockets.get(pocketNumber);
		expectingPocketSelection = false;
		PoolTurn pocketSelectedTurn = new PoolTurn();
		if (expectingHandBallUpdate) {
			pocketSelectedTurn.setTurnResult(TurnResult.HANDBALL);
		} else {
			pocketSelectedTurn.setTurnResult(TurnResult.CONTINUE_TURN);
		}
		pocketSelectedTurn.setSelectedPocket(pocketNumber);
		pocketSelectedTurn.setCueBall(this.cueBall);
		pocketSelectedTurn.setPlayer(user.getName());
		LOGGER.info(user.getName() + " selectedPocket:" + pocketNumber);
		return pocketSelectedTurn;
	}

	public boolean handBallPositionAllowed(CueBall cueBall) {
		Boundry left = this.boundries.get(5);
		Boundry right = this.boundries.get(2);
		Boundry topLeft = this.boundries.get(0);
		Boundry bottomRight = this.boundries.get(3);
		double x = cueBall.getPosition().x;
		double y = cueBall.getPosition().y;
		double r = cueBall.getRadius();
		if (x > left.a + r && x < right.a - r) {
			LOGGER.fine("boundry1");
			if (!(y > topLeft.a + r && y < bottomRight.a - r)) {
				LOGGER.fine("boundry2");
				return false;
			}
		} else {
			LOGGER.fine("boundry3");
			return false;
		}
		for (Ball ball : remainingBalls) {
			if (eightBallRuleBase.areBallsIntersecting(ball, cueBall)) {
				return false;
			}
		}
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

	public Pocket getSelectedPocket() {
		return selectedPocket;
	}

	public void putBallInPocket(Ball ballToPocket, Pocket pocket) {
		LOGGER.info("InComing:" + ballToPocket.getNumber() + " pocket:" + pocket.getCenter() + " player:" + this.playerInTurn.getName());
		if (ballToPocket.getNumber() == 0) {
			ballToPocket.setInPocket(true);
			return;
		}
		if (ballToPocket.getNumber() == 8) {
			ballToPocket.setInPocket(true);
			pocket.setContainsEightBall(true);
		}
		ballToPocket.setInPocket(true);
		ballToPocket.setVelocity(new Vector2d(0d, 0d));
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

	@JsonbTransient
	public boolean isExpectingHandball() {
		return this.expectingHandBallUpdate;
	}

	@JsonbTransient
	public boolean isExpectingPocketSelection() {
		return this.expectingPocketSelection;
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

	public void updateCueBallPosition(Vector2d position) {
		this.cueBall.position.x = position.x;
		this.cueBall.position.y = position.y;
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

	public UUID getGameId() {
		return gameId;
	}

	public void setGameId(UUID gameId) {
		this.gameId = gameId;
	}

	@Override
	protected Table startRematch() {
		this.gameId = UUID.randomUUID();
		EightBallInitializer.init(this);
		expectingHandBallUpdate = true;
		gameOver = false;
		super.resetRematchPlayer();
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
		return super.suggestRematch(user);
	}

	public Vector2d getPosition() {
		return new Vector2d(0d, 0d);
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

	public void setEightBallRuleBase(EightBallRuleBase eightBallRuleBase) {
		this.eightBallRuleBase = eightBallRuleBase;
	}

	@Override
	public int getNumber() {
		// TODO Auto-generated method stub
		return -1;
	}

}
