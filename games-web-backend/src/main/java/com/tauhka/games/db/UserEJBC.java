package com.tauhka.games.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.tauhka.games.core.User;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;

//Checks if given UUID has a match in database. Returns userName mapped to token if any.
//Lot's of different combinations for UUID tokens -> should not be easy to guess nor enumerate.
// Code does not contain blocking conditions for enumerating UUIDs endlessly, in real app. /prod.env blocking should be implemented in code or with WAF.
@Stateless(name = "UserEJBC")
public class UserEJBC {
	private static final Logger LOGGER = Logger.getLogger(UserEJBC.class.getName());
	@Resource(name = "jdbc/MariaDb")
	private DataSource gamesDataSource;
	private static final String WEBSOCKET_AUTHENTICATION_QUEURY = "SELECT  a.player_id, b.name,b.id,b.ranking_tictactoe,b.ranking_connectfour,b.ranking_eightball FROM  login a,  user b WHERE a.id =? AND a.player_id= b.id";

	public User verifyWebsocketToken(String activeLoginToken) {
		LOGGER.info("UserEJBC verifyWebsocketToken" + activeLoginToken);
		if (activeLoginToken == null) {
			return null;
		}
		PreparedStatement stmt = null;
		Connection con = null;
		UUID token = UUID.fromString(activeLoginToken);
		if (!activeLoginToken.equals(token.toString())) {
			throw new IllegalArgumentException("User provided login input which is not UUID:" + activeLoginToken);
		}
		try {
			con = gamesDataSource.getConnection();
			stmt = con.prepareStatement(WEBSOCKET_AUTHENTICATION_QUEURY);
			stmt.setString(1, token.toString());
			ResultSet res = stmt.executeQuery();
			if (res.next()) {
				String namee = res.getString("name");
				UUID id = UUID.fromString(res.getString("player_id"));
				User user = new User();
				user.setName(namee);
				user.setId(id);
				user.setRankingConnectFour(res.getDouble("ranking_connectfour"));
				user.setRankingTictactoe(res.getDouble("ranking_tictactoe"));
				user.setRankingEightBall(res.getDouble("ranking_eightball"));
				return user;
			}
			// Note: ActiveLogin token is directly connected with error message in order
			// to prevent log injections.
			throw new IllegalArgumentException("UserEJBA verifyWebsocketToken: no result for:" + activeLoginToken);

		} catch (SQLException e) {
			LOGGER.severe("UserEJBA verifyWebsocketToken sqle" + e.getMessage() + e.getErrorCode());
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException e) {
				LOGGER.log(Level.SEVERE, " UserEJBA verifyWebsocketToken finally crashed" + e);
			}
		}
		throw new RuntimeException("UserEJBA websocket runtimeException:" + token);
	}
}
