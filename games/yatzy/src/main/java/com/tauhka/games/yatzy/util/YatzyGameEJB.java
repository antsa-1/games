package com.tauhka.games.yatzy.util;

import static com.tauhka.games.core.util.Constants.LOG_PREFIX_GAMES;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import com.tauhka.games.core.stats.MultiplayerRankingCalculator;
import com.tauhka.games.core.stats.Player;
import com.tauhka.games.core.stats.Result;
import com.tauhka.games.core.timer.TimeControlIndex;
import com.tauhka.games.core.util.Constants;

import jakarta.annotation.Resource;
import jakarta.ejb.AccessTimeout;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.Stateless;

/**
 * @author antsa-1 from GitHub 27 Jun 2022
 **/

@Stateless(name = "YatzyGameEJB")
public class YatzyGameEJB {
	@Resource(name = "jdbc/MariaDb")
	private DataSource yatzyDatasource;
	private static final String INSERT_GAME_SQL = "INSERT INTO yatzy_game (start_time,end_time, game_id, game_type,player1_name,player2_name,player3_name,player4_name,player1_start_ranking,player1_end_ranking,player2_start_ranking,player2_end_ranking,player3_start_ranking,player3_end_ranking,player4_start_ranking,player4_end_ranking,player1_score,player2_score,player3_score,player4_score,player1_id,player2_id,player3_id,player4_id, timecontrol_seconds) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private static final Logger LOGGER = Logger.getLogger(YatzyGameEJB.class.getName());

	@AccessTimeout(15000)
	@Asynchronous
	public void saveYatzyGame(Result result) {
		if (result.isComplete()) {
			return;
		}
		PreparedStatement stmt = null;
		Connection con = null;
		if (!result.isComplete()) {
			result.setComplete(true);
			fetchInitialRankings(result);
			MultiplayerRankingCalculator.calculateNewRankings(result);
		}
		try {
			con = yatzyDatasource.getConnection();
			stmt = con.prepareStatement(INSERT_GAME_SQL);
			stmt.setTimestamp(1, Timestamp.from(result.getStartInstant()));
			stmt.setTimestamp(2, Timestamp.from(result.getEndInstant()));
			stmt.setString(3, result.getGameId().toString());
			stmt.setInt(4, result.getGameMode().getId());
			stmt.setString(5, result.getPlayers().get(0).getName());
			stmt.setString(6, result.getPlayers().get(1).getName());
			addPlayerIfExist(stmt, 7, result.getPlayers(), 2);
			addPlayerIfExist(stmt, 8, result.getPlayers(), 3);
			addInitialRanking(stmt, 9, result.getPlayers(), 0);
			addFinalRanking(stmt, 10, result.getPlayers(), 0);
			addInitialRanking(stmt, 11, result.getPlayers(), 1);
			addFinalRanking(stmt, 12, result.getPlayers(), 1);
			addInitialRanking(stmt, 13, result.getPlayers(), 2);
			addFinalRanking(stmt, 14, result.getPlayers(), 2);
			addInitialRanking(stmt, 15, result.getPlayers(), 3);
			addFinalRanking(stmt, 16, result.getPlayers(), 3);
			setNullOrInteger(stmt, 17, result.getPlayers().get(0).getScore());
			setNullOrInteger(stmt, 18, result.getPlayers().get(1).getScore());
			addPlayerScore(stmt, 19, result.getPlayers(), 2);
			addPlayerScore(stmt, 20, result.getPlayers(), 3);
			addPlayerId(stmt, 21, result.getPlayers(), 0);
			addPlayerId(stmt, 22, result.getPlayers(), 1);
			addPlayerId(stmt, 23, result.getPlayers(), 2);
			addPlayerId(stmt, 24, result.getPlayers(), 3);
			stmt.setInt(25, result.getTimeControlIndex().getSeconds());
			int dbRes = stmt.executeUpdate();
			if (dbRes > 0) {
				updateLatestRankings(result);
				LOGGER.info(LOG_PREFIX_GAMES + "YatzyGameEJB updated game result:" + result);
				return;
			}
			LOGGER.severe(LOG_PREFIX_GAMES + "YatzyGameEJB failed to write to db:" + result);
			return;
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "YatzyGameEJB sqle:" + result, e);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "YatzyGameEJB saveYatzyTurn exception data:" + result, e);
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException e) {
				LOGGER.log(Level.SEVERE, "YatzyGameEJB finally crashed", e);
			}
		}

	}

	private String getDbColumName(Result result) {
		TimeControlIndex index = result.getTimeControlIndex();
		if (index.getSeconds() == 10) {
			return Constants.YATZY_HYPER;
		}
		if (index.getSeconds() == 15) {
			return Constants.YATZY_SUPER;
		}
		if (index.getSeconds() == 20) {
			return Constants.YATZY_FAST;
		}
		return Constants.YATZY_CLASSIC;
	}

	private void updateLatestRankings(Result result) {
		LOGGER.info("YatzyGameEJB Updating latest rankings");
		if (result.getRankingPlayers().size() < 1) { // 2 at least at runtime
			LOGGER.info("YatzyGameEJB no final rankings");
			return;
		}
		PreparedStatement stmt = null;
		Connection con = null;
		try {
			String column = getDbColumName(result);
			String sql = String.format("insert into rankings(player_id, player_name," + column + ") values (%s",
					result.getRankingPlayers().stream().filter(player -> player.getFinalRanking() != null).map(values -> "?,?,?)").collect(Collectors.joining(",(")));
			sql += "ON DUPLICATE KEY UPDATE " + column + " =values(" + column + ")";
			con = yatzyDatasource.getConnection();
			stmt = con.prepareStatement(sql);
			int playerIndex = 0;
			for (int i = 1; i < result.getRankingPlayers().size() * 3; i = i + 3) {
				stmt.setString(i, result.getRankingPlayers().get(playerIndex).getId().toString());
				stmt.setString(i + 1, result.getRankingPlayers().get(playerIndex).getName());
				stmt.setDouble(i + 2, result.getRankingPlayers().get(playerIndex).getFinalRanking());
				playerIndex++;
			}
			int dbRes = stmt.executeUpdate();
			if (dbRes > 0) {
				LOGGER.info(LOG_PREFIX_GAMES + "YatzyGameEJB updated latest rankings:" + result);
				return;
			}
			LOGGER.severe(LOG_PREFIX_GAMES + "YatzyGameEJB updateLatest rankings failed to write to db:" + result);
			return;
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "YatzyGameEJB updateLatest sqle: data:" + result, e);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "YatzyGameEJB updateLatest exception data:" + result, e);
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException e) {
				LOGGER.log(Level.SEVERE, "YatzyGameEJB finally crashed", e);
			}
		}
	}

	private void addPlayerId(PreparedStatement preparedStatement, int index, List<Player> players, int nth) throws SQLException {
		if (nth >= players.size() || players.get(nth).getId() == null) {
			preparedStatement.setNull(index, java.sql.Types.NULL);
			return;
		}
		preparedStatement.setString(index, players.get(nth).getId().toString());
	}

	@AccessTimeout(10000)
	private void fetchInitialRankings(Result result) {
		PreparedStatement stmt = null;
		Connection con = null;
		LOGGER.info("YatzyStatsEJB starting update initialRankings");
		try {
			Stream<Player> playerStream = result.getPlayers().stream();
			List<Player> playersWithId = playerStream.filter(player -> player.getId() != null).collect(Collectors.toList());
			if (playersWithId.size() < 2) {
				// No ranking points will be calculated
				return;
			}
			playerStream = result.getPlayers().stream();
			con = yatzyDatasource.getConnection();
			String column = result.getTimeControlIndex().getSeconds() > 20 ? "yatzy" : getDbColumName(result);
			String readRankingsSQL = String.format("select " + column + ", player_id from rankings where player_id in (%s)", playerStream.filter(player -> player.getId() != null).map(values -> "?").collect(Collectors.joining(", ")));
			stmt = con.prepareStatement(readRankingsSQL);
			for (int i = 0; i < playersWithId.size(); i++) {
				stmt.setString(i + 1, playersWithId.get(i).getId().toString());
			}
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Double ranking = rs.getDouble(1);
				String id = rs.getString(2);
				for (Player p : result.getPlayers()) {
					if (p.getId() != null && p.getId().toString().equals(id)) {
						if (ranking <= 0) {
							ranking = 1000d;// losing all points leads back to 1000
						}
						p.setInitialRanking(ranking);
					}
				}
			}
			rs.close();
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "YatzyStatsEJB readInitialRankings sqle:" + e + " data:", result);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "YatzyStatsEJB readInitialRankings exception data:" + result, e);
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException e) {
				LOGGER.log(Level.SEVERE, "YatzyStatsEJB readInitialRankings finally crashed", e);
			}

		}
		return;
	}

	private void addInitialRanking(PreparedStatement preparedStatement, int index, List<Player> players, int nth) throws SQLException {
		if (nth >= players.size() || players.get(nth).getInitialRanking() == null) {
			preparedStatement.setNull(index, java.sql.Types.NULL);
			return;
		}
		preparedStatement.setDouble(index, players.get(nth).getInitialRanking());
	}

	private void addFinalRanking(PreparedStatement preparedStatement, int index, List<Player> players, int nth) throws SQLException {
		if (nth >= players.size() || players.get(nth).getFinalRanking() == null) {
			preparedStatement.setNull(index, java.sql.Types.NULL);
			return;
		}
		preparedStatement.setDouble(index, players.get(nth).getFinalRanking());
	}

	private void addPlayerIfExist(PreparedStatement preparedStatement, int index, List<Player> players, int nth) throws SQLException {
		if (nth >= players.size()) {
			preparedStatement.setNull(index, java.sql.Types.NULL);
			return;
		}
		preparedStatement.setString(index, players.get(nth).getName());
	}

	private void setNullOrInteger(PreparedStatement predparedStatement, int index, Integer val) throws SQLException {
		if (val == null) {
			predparedStatement.setNull(index, java.sql.Types.NULL);
			return;
		}
		predparedStatement.setInt(index, val);
	}

	public void addPlayerScore(PreparedStatement stmt, int index, List<Player> players, int nthPlayer) throws SQLException {
		if (nthPlayer >= players.size()) {
			setNullOrInteger(stmt, index, null);
			return;
		}
		int score = players.get(nthPlayer).getScore();
		setNullOrInteger(stmt, index, score);
	}
}
