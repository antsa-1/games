package com.tauhka.games.ejb;

import static com.tauhka.games.core.util.Constants.ANONYM_LOGIN_NAME_START;
import static com.tauhka.games.core.util.Constants.LOG_PREFIX_GAMES;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.tauhka.games.core.stats.GameStatisticsEvent;
import com.tauhka.games.core.twodimen.GameResult;

import jakarta.annotation.Resource;
import jakarta.ejb.EJBContext;
import jakarta.ejb.Stateless;
import jakarta.enterprise.event.ObservesAsync;

@Stateless
public class StatisticsEJB { // To core package?!?!?

	private static final Logger LOGGER = Logger.getLogger(StatisticsEJB.class.getName());
	private static final String INSERT_GAME_RESULT_SQL = "INSERT INTO statistics_games (playerA_id, playerB_id, winner_id, game_id, game_type,start_time,end_time, result,playerA_username,playerB_username) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

	@Resource(name = "jdbc/MariaDb")
	private DataSource gamesDataSource;
	@Resource
	private EJBContext ejbContext;

	public void observeGameResult(@ObservesAsync GameStatisticsEvent gameStats) {
		// If Anonym player then do not update..
		LOGGER.info("StatisticsEJB should update now database with " + gameStats);
		if (gameStats == null || gameStats.getGameResult() == null) {
			throw new IllegalArgumentException("No statistics for database:" + gameStats);// Tells which one was null
		}
		if (isRegisteredPlayers(gameStats)) {
			insertGameResultToDatabase(gameStats); //Transactions?
			updatePlayerRankings(gameStats); // DB trigger after insert?
			updateRankingsInDatabase(gameStats);
		}

	}

	private void updatePlayedGamesAmount() {
		// TODO Auto-generated method stub

	}

	private void updateRankingsInDatabase(GameStatisticsEvent gameStats) {
		// TODO Auto-generated method stub

	}

	private void updatePlayerRankings(GameStatisticsEvent gameStats) {
		// Calculate rankings

	}

	private boolean atLeastOnePlayerHasNickname(GameStatisticsEvent gameStats) {
		return !gameStats.getGameResult().getPlayerA().getName().startsWith(ANONYM_LOGIN_NAME_START) || !gameStats.getGameResult().getPlayerB().getName().startsWith(ANONYM_LOGIN_NAME_START);
	}

	private boolean isRegisteredPlayers(GameStatisticsEvent gameStats) {
		return !gameStats.getGameResult().getPlayerA().getName().startsWith(ANONYM_LOGIN_NAME_START) && !gameStats.getGameResult().getPlayerB().getName().startsWith(ANONYM_LOGIN_NAME_START);
	}

	private void insertGameResultToDatabase(GameStatisticsEvent gameEvent) {
		GameResult res = gameEvent.getGameResult();
		PreparedStatement stmt = null;
		Connection con = null;
		try {
			con = gamesDataSource.getConnection();
			stmt = con.prepareStatement(INSERT_GAME_RESULT_SQL);
			stmt.setString(1, res.getPlayerA().getId().toString());
			stmt.setString(2, res.getPlayerB().getId().toString());
			// Winner, can be null if draw
			stmt.setString(3, res.getPlayer() == null ? null : res.getPlayer().getId().toString());
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
				LOGGER.severe(LOG_PREFIX_GAMES + "UserEJB finally crashed" + e.getMessage());
			}
		}
	}
}
