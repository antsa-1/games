package com.tauhka.games.ejb;

import static com.tauhka.games.core.util.Constants.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.User;
import com.tauhka.games.core.ai.AI;
import com.tauhka.games.core.ai.ArtificialUser;
import com.tauhka.games.core.stats.GameStatisticsEvent;
import com.tauhka.games.core.stats.RankingCalculator;
import com.tauhka.games.core.twodimen.GameResult;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;

@Stateless
public class StatisticsEJB { // To core package?!?!?

	private static final Logger LOGGER = Logger.getLogger(StatisticsEJB.class.getName());
	private static final String INSERT_GAME_RESULT_SQL = "INSERT INTO game (playera_id, playerb_id, winner_id, game_id, game_type,start_time,end_time, result,playera_username,playerb_username,playera_start_ranking,playera_end_ranking,playerb_start_ranking,playerb_end_ranking,winner_username) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?,?);";
	private static final String UPDATE_TICTACTOE_GAMES_COUNT_SQL = "UPDATE game_counter SET tictactoes= tictactoes+1";
	private static final String UPDATE_CONNECT_FOUR_GAMES_COUNT_SQL = "UPDATE game_counter SET connectfours= connectfours+1";
	private static final String UPDATE_EIGHT_BALL_GAMES_COUNT_SQL = "UPDATE game_counter SET eightballs= eightballs+1";
	private static final String UPDATE_TICTACTOE_PLAYER_RANKINGS_SQL = "UPDATE user SET ranking_tictactoe = CASE WHEN id =? THEN ? WHEN id = ? THEN ? END WHERE id IN (?,?)";
	private static final String UPDATE_CONNECT_FOUR_PLAYER_RANKINGS_SQL = "UPDATE user SET ranking_connectfour = CASE WHEN id =? THEN ? WHEN id = ? THEN ? END WHERE id IN (?,?)";
	private static final String UPDATE_EIGHT_BALL_PLAYER_RANKINGS_SQL = "UPDATE user SET ranking_eightball = CASE WHEN id =? THEN ? WHEN id = ? THEN ? END WHERE id IN (?,?)";

	@Resource(name = "jdbc/MariaDb")
	private DataSource gamesDataSource;

	public void observeGameResult(@ObservesAsync GameStatisticsEvent gameStats) {

		LOGGER.info("StatisticsEJB should async update now database with " + gameStats);
		updateDatabase(gameStats);
	}

	private void updateDatabase(GameStatisticsEvent gameStats) {
		if (gameStats == null || gameStats.getGameResult() == null) {
			throw new IllegalArgumentException("No statistics for database:" + gameStats);// Tells which one was null
		}
		if (isBothPlayersLoggedIn(gameStats)) {
			LOGGER.info("StatisticsEJB Both players loggedIn");
			RankingCalculator.updateRankings(gameStats.getGameResult());
			insertGameResultToDatabase(gameStats);
			updatePlayerRankingsToDatabase(gameStats);
		} else if (isOneRegisteredPlayer(gameStats)) {
			LOGGER.info("StatisticsEJB One player loggedIn");
			setupStartRankingsWithoutCalculations(gameStats);
			insertGameResultToDatabase(gameStats);
		} else {
			LOGGER.info("StatisticsEJB No loggedInPlayers");
			updateGameCount(gameStats);
		}
	}

	public void observeGameResultSync(@Observes GameStatisticsEvent gameStats) {
		LOGGER.info("StatisticsEJB should sync update now database with " + gameStats);
		updateDatabase(gameStats);
	}

	// Updates User-table
	private void updatePlayerRankingsToDatabase(GameStatisticsEvent gameStats) {

		GameResult res = gameStats.getGameResult();
		PreparedStatement stmt = null;
		Connection con = null;
		try {
			con = gamesDataSource.getConnection();
			String sql = getUpdateRankingSQL(res.getGameMode());
			stmt = con.prepareStatement(sql);
			User userA = res.getPlayerA();
			User userB = res.getPlayerB();
			Double rankingA = userA.getRanking(res.getGameMode());
			Double rankingB = userB.getRanking(res.getGameMode());

			stmt.setString(1, userA.getId() != null ? userA.getId().toString() : null);
			stmt.setDouble(2, rankingA);
			stmt.setString(3, userB.getId() != null ? userB.getId().toString() : null);
			stmt.setDouble(4, rankingB);
			stmt.setString(5, userA.getId() != null ? userA.getId().toString() : null);
			stmt.setString(6, userB.getId() != null ? userB.getId().toString() : null);
			int dbRes = stmt.executeUpdate();
			if (dbRes > 0) {
				LOGGER.info(LOG_PREFIX_GAMES + "StatisticsEJB updated PlayerRankingsToDatabase:" + gameStats);
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

	private String getUpdateRankingSQL(GameMode gameMode) {
		if (gameMode.isConnectFour()) {
			return UPDATE_CONNECT_FOUR_PLAYER_RANKINGS_SQL;
		}
		if (gameMode.isTicTacToe()) {
			return UPDATE_TICTACTOE_PLAYER_RANKINGS_SQL;
		}
		return UPDATE_EIGHT_BALL_PLAYER_RANKINGS_SQL;
	}

	// No user input in this sql
	private void updateGameCount(GameStatisticsEvent gameStats) {
		PreparedStatement stmt = null;
		Connection con = null;
		int gameNumber = gameStats.getGameResult().getGameMode().getGameNumber();
		try {
			con = gamesDataSource.getConnection();
			stmt = con.prepareStatement(getGameCountSQL(gameStats.getGameResult().getGameMode()));
			int dbRes = stmt.executeUpdate();
			if (dbRes > 0) {
				LOGGER.info(LOG_PREFIX_GAMES + "StatisticsEJB updated gameAmounts for guest players game" + gameNumber);
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

	private String getGameCountSQL(GameMode mode) {
		if (mode.isConnectFour()) {
			return UPDATE_CONNECT_FOUR_GAMES_COUNT_SQL;
		}
		if (mode.isEightBall()) {
			return UPDATE_EIGHT_BALL_GAMES_COUNT_SQL;
		}
		if (mode.isTicTacToe()) {
			return UPDATE_TICTACTOE_GAMES_COUNT_SQL;
		}
		throw new IllegalArgumentException("No sql for gameMode:" + mode);
	}

	private boolean isBothPlayersLoggedIn(GameStatisticsEvent gameStats) {
		User playerA = gameStats.getGameResult().getPlayerA();
		User playerB = gameStats.getGameResult().getPlayerB();
		boolean computerPlayer = playerB instanceof ArtificialUser;
		// Computer sits always on playerB position
		return !playerA.getName().startsWith(GUEST_LOGIN_NAME) && !playerB.getName().startsWith(GUEST_LOGIN_NAME) && !computerPlayer;
	}

	private boolean isOneRegisteredPlayer(GameStatisticsEvent gameStats) {
		User playerA = gameStats.getGameResult().getPlayerA();
		User playerB = gameStats.getGameResult().getPlayerB();
		boolean computerPlayer = playerB instanceof AI;
		// Computer sits always on playerB position
		return !playerA.getName().startsWith(GUEST_LOGIN_NAME) || !playerB.getName().startsWith(GUEST_LOGIN_NAME);
	}

	private void insertGameResultToDatabase(GameStatisticsEvent gameEvent) {
		GameResult res = gameEvent.getGameResult();
		PreparedStatement stmt = null;
		Connection con = null;
		try {
			con = gamesDataSource.getConnection();
			stmt = con.prepareStatement(INSERT_GAME_RESULT_SQL);
			User userA = res.getPlayerA();
			User userB = res.getPlayerB();
			UUID winnerId = res.getWinner() != null ? res.getWinner().getId() : null;
			Double endRankingA = userA.getRanking(gameEvent.getGameResult().getGameMode());
			Double endRankingB = userB.getRanking(gameEvent.getGameResult().getGameMode());
			stmt.setString(1, userA.getId() != null ? userA.getId().toString() : null);
			stmt.setString(2, userB.getId() != null ? userB.getId().toString() : null);
			// Winner, can be null if draw
			stmt.setString(3, winnerId != null ? winnerId.toString() : null);
			stmt.setString(4, res.getGameId().toString());
			stmt.setInt(5, res.getGameMode().getId());
			stmt.setTimestamp(6, Timestamp.from(res.getStartInstant()));
			stmt.setTimestamp(7, Timestamp.from(res.getEndInstant()));
			stmt.setInt(8, res.getResultType().getAsInt());
			stmt.setString(9, userA.getName());
			stmt.setString(10, userB.getName());
			stmt.setDouble(11, userA.getInitialCalculationsRank());
			stmt.setDouble(12, endRankingA);
			stmt.setDouble(13, userB.getInitialCalculationsRank());
			stmt.setDouble(14, endRankingB);
			stmt.setString(15, res.getWinner() != null ? res.getWinner().getName() : null);
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

	private void setupStartRankingsWithoutCalculations(GameStatisticsEvent gameStats) {
		GameResult result = gameStats.getGameResult();
		User playerA = result.getPlayerA();
		User playerB = result.getPlayerB();
		Double aRankingInitial = playerA.getRanking(gameStats.getGameResult().getGameMode());
		Double bRankingInitial = playerB.getRanking(gameStats.getGameResult().getGameMode());
		playerA.setInitialCalculationsRank(aRankingInitial);
		playerB.setInitialCalculationsRank(bRankingInitial);
	}
}
