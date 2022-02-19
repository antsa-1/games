package com.tauhka.games.ejb;

import static com.tauhka.games.core.util.Constants.ANONYM_LOGIN_NAME_START;
import static com.tauhka.games.core.util.Constants.LOG_PREFIX_GAMES;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.tauhka.games.core.User;
import com.tauhka.games.core.stats.GameStatisticsEvent;
import com.tauhka.games.core.stats.RankingCalculator;
import com.tauhka.games.core.twodimen.GameResult;

import jakarta.annotation.Resource;
import jakarta.ejb.EJBContext;
import jakarta.ejb.Stateless;
import jakarta.enterprise.event.ObservesAsync;

@Stateless
public class StatisticsEJB { // To core package?!?!?

	private static final Logger LOGGER = Logger.getLogger(StatisticsEJB.class.getName());
	private static final String INSERT_GAME_RESULT_SQL = "INSERT INTO statistics_games (playerA_id, playerB_id, winner_id, game_id, game_type,start_time,end_time, result,playerA_username,playerB_username) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	private static final String UPDATE_TICTACTOE_GAMES_COUNT_SQL = "UPDATE statistics_game_counts SET tictactoes= tictactoes+1";
	private static final String UPDATE_CONNECT_FOUR_GAMES_COUNT_SQL = "UPDATE statistics_game_counts SET connectfours= connectfours+1";
	private static final String UPDATE_TICTACTOE_PLAYER_RANKINGS_SQL = "UPDATE users SET ranking_tictactoe = CASE WHEN Id =? THEN ? WHEN Id = ? THEN ? END WHERE ID IN (?,?)";
	private static final String UPDATE_CONNECT_FOUR_PLAYER_RANKINGS_SQL = "UPDATE users SET ranking_connect_four = CASE WHEN Id =? THEN ? WHEN Id = ? THEN ? END WHERE ID IN (?,?)";

	@Resource(name = "jdbc/MariaDb")
	private DataSource gamesDataSource;
	@Resource
	private EJBContext ejbContext;

	public void observeGameResult(@ObservesAsync GameStatisticsEvent gameStats) {

		LOGGER.info("StatisticsEJB should update now database with " + gameStats);
		if (gameStats == null || gameStats.getGameResult() == null) {
			throw new IllegalArgumentException("No statistics for database:" + gameStats);// Tells which one was null
		}
		if (isAtLeastOneRegisteredPlayerinTheGame(gameStats)) {
			insertGameResultToDatabase(gameStats); // DB trigger connected to update game counts
			RankingCalculator.updateRankings(gameStats.getGameResult());
			updatePlayerRankingsToDatabase(gameStats);
		} else {
			// Anonym players play together
			updateGameCount(gameStats);
		}
	}

	private void updatePlayerRankingsToDatabase(GameStatisticsEvent gameStats) {
		GameResult res = gameStats.getGameResult();
		PreparedStatement stmt = null;
		Connection con = null;
		try {
			boolean connectFour = res.getGameMode().isConnectFour();
			con = gamesDataSource.getConnection();
			String sql = connectFour ? UPDATE_CONNECT_FOUR_PLAYER_RANKINGS_SQL : UPDATE_TICTACTOE_PLAYER_RANKINGS_SQL;
			stmt = con.prepareStatement(sql);
			User userA = res.getPlayerA();
			User userB = res.getPlayerB();
			Double rankingA = connectFour ? userA.getRankingConnectFour() : userA.getRankingTictactoe();
			Double rankingB = connectFour ? userB.getRankingConnectFour() : userB.getRankingTictactoe();

			stmt.setString(1, userA.getId().toString());
			stmt.setDouble(2, rankingA);
			stmt.setString(3, userB.getId().toString());
			stmt.setDouble(4, rankingB);
			stmt.setString(5, userA.getId().toString());
			stmt.setString(6, userB.getId().toString());
			int dbRes = stmt.executeUpdate();
			if (dbRes > 0) {
				LOGGER.info(LOG_PREFIX_GAMES + "StatisticsEJB inserted new game result:" + gameStats);
				return;
			}
			// Do nothing, update manually from log or put to error queue?
			LOGGER.severe(LOG_PREFIX_GAMES + "StatisticsEJB failed to write to db:" + gameStats);
			return;
		} catch (SQLException e) {
			LOGGER.severe(LOG_PREFIX_GAMES + "StatisticsEJB sqle" + e.getMessage());
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException e) {
				LOGGER.severe(LOG_PREFIX_GAMES + "StatisticsEJB finally crashed" + e.getMessage());
			}
		}
	}

	// No user input in this sql
	private void updateGameCount(GameStatisticsEvent gameStats) {
		PreparedStatement stmt = null;
		Connection con = null;
		int gameNumber = gameStats.getGameResult().getGameMode().getGameNumber();
		try {
			con = gamesDataSource.getConnection();
			stmt = con.prepareStatement(gameNumber == 1 ? UPDATE_TICTACTOE_GAMES_COUNT_SQL : UPDATE_CONNECT_FOUR_GAMES_COUNT_SQL);
			int dbRes = stmt.executeUpdate();
			if (dbRes > 0) {
				LOGGER.info(LOG_PREFIX_GAMES + "StatisticsEJB updated gameAmounts for anonymous players game" + gameNumber);
				return;
			}

			LOGGER.severe(LOG_PREFIX_GAMES + "StatisticsEJB failed to update gameAmounts:" + gameNumber);
			return;
		} catch (SQLException e) {
			LOGGER.severe(LOG_PREFIX_GAMES + "StatisticsEJB sqle" + e.getMessage());
		} finally {
			try {
				if (con != null) {
					con.close();
				}
			} catch (SQLException e) {
				LOGGER.severe(LOG_PREFIX_GAMES + "StatisticsEJB finally crashed" + e.getMessage());
			}
		}

	}

	private boolean isAtLeastOneRegisteredPlayerinTheGame(GameStatisticsEvent gameStats) {
		return !gameStats.getGameResult().getPlayerA().getName().startsWith(ANONYM_LOGIN_NAME_START) || !gameStats.getGameResult().getPlayerB().getName().startsWith(ANONYM_LOGIN_NAME_START);
	}

	private void insertGameResultToDatabase(GameStatisticsEvent gameEvent) {
		GameResult res = gameEvent.getGameResult();
		PreparedStatement stmt = null;
		Connection con = null;
		try {
			con = gamesDataSource.getConnection();
			stmt = con.prepareStatement(INSERT_GAME_RESULT_SQL);
			UUID playerAId = res.getPlayerA().getId();
			UUID playerBId = res.getPlayerB().getId();
			UUID winnerId = res.getPlayer().getId();
			stmt.setString(1, playerAId != null ? playerAId.toString() : null);
			stmt.setString(2, playerBId != null ? playerBId.toString() : null);
			// Winner, can be null if draw
			stmt.setString(3, winnerId != null ? winnerId.toString() : null);
			stmt.setString(4, res.getGameId().toString());
			stmt.setInt(5, res.getGameMode().getId());
			stmt.setTimestamp(6, Timestamp.from(res.getStartInstant()));
			stmt.setTimestamp(7, Timestamp.from(res.getEndInstant()));
			stmt.setInt(8, res.getResultType().getAsInt());
			stmt.setString(9, res.getPlayerA().getName());
			stmt.setString(10, res.getPlayerB().getName());
			int dbRes = stmt.executeUpdate();
			if (dbRes > 0) {
				LOGGER.info(LOG_PREFIX_GAMES + "StatisticsEJB inserted new game result:" + gameEvent);
				return;
			}
			// Do nothing, update manually from log or put to error queue?
			LOGGER.severe(LOG_PREFIX_GAMES + "StatisticsEJB failed to write to db:" + gameEvent);
			return;
		} catch (SQLException e) {
			LOGGER.severe(LOG_PREFIX_GAMES + "StatisticsEJB sqle" + e.getMessage());
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException e) {
				LOGGER.severe(LOG_PREFIX_GAMES + "StatisticsEJB finally crashed" + e.getMessage());
			}
		}
	}
}
