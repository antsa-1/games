package com.tauhka.games.db;

import static com.tauhka.games.core.util.Constants.LOG_PREFIX_GAMES;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.tauhka.games.core.stats.Player;
import com.tauhka.games.db.dao.YatzyTurnDao;

import jakarta.annotation.Resource;
import jakarta.ejb.AccessTimeout;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.Stateless;

/**
 * @author antsa-1 from GitHub 27 Jun 2022
 **/

@Stateless(name = "YatzyEventEJB")
public class YatzyEventEJB {
	@Resource(name = "jdbc/MariaDb")
	private DataSource yatzyDatasource;
	private static final String INSERT_EVENT_SQL = "INSERT INTO yatzy_event (player_name,player_id, game_id, event_type, dice1, dice2, dice3, dice4, dice5, hand_score,selected_hand) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
//	private static final String INSERT_GAME_SQL = "INSERT INTO yatzy_game (start_time,end_time, game_id, game_type,player1_name,player2_name,player3_name,player4_name,player1_start_ranking,player1_end_ranking,player2_start_ranking,player2_end_ranking,player3_start_ranking,player3_end_ranking,player4_start_ranking,player4_end_ranking,player1_score,player2_score,player3_score,player4_score) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private static final Logger LOGGER = Logger.getLogger(YatzyEventEJB.class.getName());

	@SuppressWarnings("exports")
	@AccessTimeout(15000)
	@Asynchronous
	public void saveYatzyTurn(YatzyTurnDao yatzyTurnDao) {

		PreparedStatement stmt = null;
		Connection con = null;
		try {
			con = yatzyDatasource.getConnection();
			boolean rollAction = yatzyTurnDao.getHandType() == null;
			stmt = con.prepareStatement(INSERT_EVENT_SQL);
			stmt.setString(1, yatzyTurnDao.getPlayerName());
			setNullOrString(stmt, 2, yatzyTurnDao.getPlayerId());
			stmt.setString(3, yatzyTurnDao.getGameId());
			stmt.setString(4, rollAction ? "ROLL" : "SELECT_HAND");
			setNullOrInteger(stmt, 5, yatzyTurnDao.getDice1Value());
			setNullOrInteger(stmt, 6, yatzyTurnDao.getDice2Value());
			setNullOrInteger(stmt, 7, yatzyTurnDao.getDice3Value());
			setNullOrInteger(stmt, 8, yatzyTurnDao.getDice4Value());
			setNullOrInteger(stmt, 9, yatzyTurnDao.getDice5Value());
			setNullOrInteger(stmt, 10, yatzyTurnDao.getScore() != null ? yatzyTurnDao.getScore() : null);
			setNullOrString(stmt, 11, yatzyTurnDao.getHandType() != null ? yatzyTurnDao.getHandType().toString() : null);
			int dbRes = stmt.executeUpdate();
			if (dbRes > 0) {
				LOGGER.info(LOG_PREFIX_GAMES + "YatzyStatsEJB updated turn:" + yatzyTurnDao);
				return;
			}
			// Do nothing, update manually from log or put to error queue?
			LOGGER.severe(LOG_PREFIX_GAMES + "YatzyStatsEJB failed to write to db:" + yatzyTurnDao);
			return;
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "YatzyStatsEJB sqle:" + e + " data:" + yatzyTurnDao);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "YatzyStatsEJB saveYatzyTurn exception data:" + yatzyTurnDao, e);
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException e) {
				LOGGER.log(Level.SEVERE, "YatzyStatsEJB finally crashed", e);
			}
		}
	}

	private void addStartRanking(PreparedStatement preparedStatement, int index, List<Player> players, int nth) throws SQLException {
		if (players.get(nth) == null) {
			preparedStatement.setNull(index, java.sql.Types.NULL);
			return;
		}
		preparedStatement.setDouble(index, players.get(nth).getInitialRanking());
	}

	private void addFinalRanking(PreparedStatement preparedStatement, int index, List<Player> players, int nth) throws SQLException {
		if (players.get(nth) == null) {
			preparedStatement.setNull(index, java.sql.Types.NULL);
			return;
		}
		preparedStatement.setDouble(index, players.get(nth).getFinalRanking());
	}

	private void addPlayerIfExist(PreparedStatement preparedStatement, int index, List<Player> players, int nth) throws SQLException {
		if (players.get(nth) == null) {
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

	public void addPlayerScore(PreparedStatement stmt, int nth, List<Player> players, int nthPlayer) throws SQLException {
		if (players.size() >= nth) {
			Integer score = players.get(nth).getScore();
			setNullOrInteger(stmt, nth, score);
			return;
		}
		stmt.setNull(nth, java.sql.Types.NULL);
	}

	private void setNullOrString(PreparedStatement predparedStatement, int index, String val) throws SQLException {
		if (val == null || val.equals("")) {
			predparedStatement.setNull(index, java.sql.Types.NULL);
			return;
		}
		predparedStatement.setString(index, val);
	}

}
