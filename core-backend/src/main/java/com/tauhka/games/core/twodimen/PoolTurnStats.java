package com.tauhka.games.core.twodimen;

import java.util.UUID;

/**
 * @author antsa-1 from GitHub 11 Apr 2022
 **/

public class PoolTurnStats {
	private Double angle;
	private Double force;
	private String playerId;
	private String playerName;
	private UUID gameId;
	private String turnResult;
	private String remainingBalls;
	private Double cueBallX;
	private Double cueBallY;
	private Integer selectedPocket;
	private String winner;
	private String winnerId;
	private String turnType;

	private PoolTurnStats(PoolTurnBuilder p) {
		this.angle = p.angle;
		this.force = p.force;
		this.playerId = p.playerId;
		this.playerName = p.playerName;
		this.gameId = p.gameId;
		this.remainingBalls = p.remainingBalls;
		this.cueBallX = p.cueBallX;
		this.cueBallY = p.cueBallY;
		this.selectedPocket = p.selectedPocket;
		this.turnResult = p.turnResult;
		this.winner = p.winner;
		this.winnerId = p.winnerId;
		this.turnType = p.turnType;
	}

	public Double getAngle() {
		return angle;
	}

	public Double getForce() {
		return force;
	}

	public String getPlayerId() {
		return playerId;
	}

	public String getTurnType() {
		return turnType;
	}

	public String getPlayerName() {
		return playerName;
	}

	public UUID getGameId() {
		return gameId;
	}

	public String getTurnResult() {
		return turnResult;
	}

	public String getRemainingBalls() {
		return remainingBalls;
	}

	public Double getCueBallX() {
		return cueBallX;
	}

	public Double getCueBallY() {
		return cueBallY;
	}

	public Integer getSelectedPocket() {
		return selectedPocket;
	}

	public String getWinner() {
		return winner;
	}

	public String getWinnerId() {
		return winnerId;
	}

	@Override
	public String toString() {
		return "PoolTurnStats [angle=" + angle + ", force=" + force + ", playerId=" + playerId + ", playerName=" + playerName + ", gameId=" + gameId + ", turnResult=" + turnResult + ", remainingBalls=" + remainingBalls + ", cueBallX="
				+ cueBallX + ", cueBallY=" + cueBallY + ", selectedPocket=" + selectedPocket + ", winner=" + winner + ", winnerId=" + winnerId + "]";
	}

	public static class PoolTurnBuilder {
		private final Double angle;
		private final Double force;
		private String playerId;
		private String playerName;
		private UUID gameId;
		private String remainingBalls;
		private Double cueBallX;
		private Double cueBallY;
		private Integer selectedPocket;
		private String turnResult;
		private String winner;
		private String winnerId;
		private String turnType;

		public PoolTurnBuilder(Double force, Double angle) {
			this.angle = angle;
			this.force = force;
		}

		public PoolTurnBuilder playerId(String playerId) {
			this.playerId = playerId;
			return this;
		}

		public PoolTurnBuilder playerName(String playerName) {
			this.playerName = playerName;
			return this;
		}

		public PoolTurnBuilder remainingBalls(String remainingBalls) {
			this.remainingBalls = remainingBalls;
			return this;
		}

		public PoolTurnBuilder winner(String winner) {
			this.winner = winner;
			return this;
		}

		public String getPlayerName() {
			return playerName;
		}

		public PoolTurnBuilder winnerId(String winnerId) {
			this.winnerId = winnerId;
			return this;
		}

		public PoolTurnBuilder cueBallX(Double cueBallX) {
			this.cueBallX = cueBallX;
			return this;
		}

		public String getWinner() {
			return winner;
		}

		public String getWinnerId() {
			return winnerId;
		}

		public PoolTurnBuilder cueBallY(Double cueBallY) {
			this.cueBallY = cueBallY;
			return this;
		}

		public PoolTurnBuilder selectedPocket(Integer selectedPocket) {
			this.selectedPocket = selectedPocket;
			return this;
		}

		public PoolTurnBuilder turnResult(String turnResult) {
			this.turnResult = turnResult;
			return this;
		}

		public PoolTurnBuilder gameId(UUID gameId) {
			this.gameId = gameId;
			return this;
		}

		public PoolTurnBuilder turnType(String turnType) {
			this.turnType = turnType;
			return this;
		}

		public PoolTurnStats build() {
			PoolTurnStats poolGameResult = new PoolTurnStats(this);
			return poolGameResult;
		}
	}
}
