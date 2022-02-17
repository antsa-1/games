package com.tauhka.games.ejb;

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
import jakarta.ejb.Stateless;
import jakarta.enterprise.event.ObservesAsync;

@Stateless
public class StatisticsEJB { // To core package?!?!?

	private static final Logger LOGGER = Logger.getLogger(StatisticsEJB.class.getName());
	private static final String INSERT_GAME_RESULT_SQL = "INSERT INTO statistics (playerA_id, playerB_id, winner_id, game_id, game_type,start_time,end_time, result) VALUES (?, ?, ?, ?, ?,?,?,?);";
	@Resource(name = "jdbc/MariaDb")
	private DataSource gamesDataSource;

	public void observeGameResult(@ObservesAsync GameStatisticsEvent gameStats) {
		// If Anonym player then do not update..
		LOGGER.info("StatisticsEJB should update now database with " + gameStats);
		if (gameStats == null || gameStats.getGameResult() == null) {
			throw new IllegalArgumentException("No statistics for database:" + gameStats);// Tells which one was null
		}
		insertGameResultToDatabase(gameStats);

	}

	private void insertGameResultToDatabase(GameStatisticsEvent gameStats) {
		GameResult res = gameStats.getGameResult();
		PreparedStatement stmt = null;
		Connection con = null;
		try {
			con = gamesDataSource.getConnection();
			stmt = con.prepareStatement(INSERT_GAME_RESULT_SQL);
			stmt.setString(1, res.getPlayerA().getName());
			stmt.setString(2, res.getPlayerB().getName());
			stmt.setString(3, res.getPlayer().getName());// Winner, can be null if draw
			stmt.setString(4, res.getGameId().toString());
			stmt.setInt(5, res.getGameMode().getGameId());
			stmt.setTimestamp(6, Timestamp.from(res.getStartInstant()));
			stmt.setTimestamp(7, Timestamp.from(res.getEndInstant()));
			stmt.setInt(8, res.getResultType().getAsInt());
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
				LOGGER.severe(LOG_PREFIX_GAMES + "UserEJB finally crashed" + e.getMessage());
			}
		}
	}
}
