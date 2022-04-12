package com.tauhka.games.ejb;

import static com.tauhka.games.core.util.Constants.LOG_PREFIX_GAMES;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.tauhka.games.core.stats.PoolGameStatisticsEvent;
import com.tauhka.games.core.twodimen.PoolTurnStats;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.enterprise.event.ObservesAsync;

/**
 * @author antsa-1 from GitHub 12 Apr 2022
 **/
@Stateless
public class PoolStatisticsEJB {
	private static final String INSERT_POOL_TURN_SQL = "INSERT INTO pool_turn (`game_id`, `player_id`, `force`, `angle`, `turn_result`, `remaining_balls`, `cueball_x`, `cueball_y`, `game_mode`,`winner`,`winner_id`,`turn_in` ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?);";
	private static final Logger LOGGER = Logger.getLogger(PoolStatisticsEJB.class.getName());
	@Resource(name = "jdbc/MariaDb")
	private DataSource gamesDataSource;

	// Idea for storing the turn data is to help check found bugs and later others could watch the games how they were played
	public void observePoolTurn(@ObservesAsync PoolGameStatisticsEvent poolTurnStats) {
		LOGGER.info("PoolStatisticsEJB should update now database with poolTurnUpdate " + poolTurnStats);
		if (poolTurnStats == null) {
			throw new IllegalArgumentException("No statistics for database:" + poolTurnStats);// Tells which one was null
		}
		updatePoolTurn(poolTurnStats.getGameResult());
	}

	private void updatePoolTurn(PoolTurnStats poolTurnResult) {
		PreparedStatement stmt = null;
		Connection con = null;
		int gameNumber = 30; // only pool game is eight ball = 30
		try {
			con = gamesDataSource.getConnection();
			String sql = INSERT_POOL_TURN_SQL;
			stmt = con.prepareStatement(sql);

			stmt.setString(1, poolTurnResult.getGameId().toString());
			stmt.setString(2, poolTurnResult.getPlayerId());
			setNullOrDouble(stmt, 3, poolTurnResult.getForce());
			setNullOrDouble(stmt, 4, poolTurnResult.getAngle());
			stmt.setString(5, poolTurnResult.getTurnResult());
			stmt.setString(6, poolTurnResult.getRemainingBalls());
			setNullOrDouble(stmt, 7, poolTurnResult.getCueBallX());
			setNullOrDouble(stmt, 8, poolTurnResult.getCueBallY());
			stmt.setInt(9, gameNumber);
			setNullOrString(stmt, 10, poolTurnResult.getWinner());
			setNullOrString(stmt, 11, poolTurnResult.getWinnerId());
			stmt.setString(12, poolTurnResult.getTurnType());
			int dbRes = stmt.executeUpdate();
			if (dbRes > 0) {
				LOGGER.info(LOG_PREFIX_GAMES + "PoolStatisticsEJB updated updatePoolTurn:" + poolTurnResult);
				return;
			}
			return;
		} catch (SQLException e) {
			LOGGER.severe(LOG_PREFIX_GAMES + "PoolStatisticsEJB sqle" + e.getMessage());
		} finally {
			try {
				if (con != null) {
					con.close();
				}
			} catch (SQLException e) {
				LOGGER.severe(LOG_PREFIX_GAMES + "PoolStatisticsEJB finally crashed" + e.getMessage());
			}
		}
	}

	private void setNullOrDouble(PreparedStatement predparedStatement, int index, Double val) throws SQLException {
		if (val == null) {
			predparedStatement.setNull(index, java.sql.Types.NULL);
			return;
		}
		predparedStatement.setDouble(index, val);
	}

	private void setNullOrString(PreparedStatement predparedStatement, int index, String val) throws SQLException {
		if (val == null) {
			predparedStatement.setNull(index, java.sql.Types.NULL);
			return;
		}
		predparedStatement.setString(index, val);
	}
}
