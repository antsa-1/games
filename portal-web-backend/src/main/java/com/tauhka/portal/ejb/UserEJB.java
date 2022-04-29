package com.tauhka.portal.ejb;
import static com.tauhka.games.core.util.Constants.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.tauhka.games.core.util.Constants;
import com.tauhka.portal.highscore.TopLists;
import com.tauhka.portal.highscore.TopPlayer;
import com.tauhka.portal.login.LoginOutput;
import com.tauhka.portal.web.security.PasswordHash;

import jakarta.annotation.Resource;
import jakarta.ejb.DuplicateKeyException;
import jakarta.ejb.Stateless;

//DB connection with register, login and logout functions
@Stateless(name = "UserEJB")
public class UserEJB {
	private static final Logger LOGGER = Logger.getLogger(UserEJB.class.getName());
	@Resource(name = "jdbc/MariaDb")
	private DataSource portalDatasource;

	private static final String USERNAME_QUERY = "select name,status,force_password_change,id,salt,password from user where name=?";
	private static final String ACTIVE_LOGIN_INSERT = "insert into login(id,player_id, user_name) values (?,?,?) ON DUPLICATE KEY UPDATE id=?";
	private static final String REGISTER_INSERT = "insert into user(name,id,email,password,salt) values (?,?,?,?,?)";
	private static final String REMOVE_ACTIVE_LOGIN = "delete from login where id= (?)";
	private static final String TOP_TICTACTOE_PLAYERS_SQL = "SELECT DISTINCT name,ranking_tictactoe FROM user ORDER BY ranking_tictactoe DESC LIMIT 20;";
	private static final String TOP_EIGHT_BALL_PLAYERS_SQL = "SELECT DISTINCT name,ranking_eightball FROM user ORDER BY ranking_eightball DESC LIMIT 20;";
	private static final String TOP_CONNECT_FOUR_PLAYERS_SQL = "SELECT DISTINCT name,ranking_connectfour FROM user ORDER BY ranking_connectfour DESC LIMIT 20;";
	private static final String SELECT_GAME_COUNTS_SQL = "SELECT connectfours,tictactoes,eightballs FROM game_counter";

	public LoginOutput login(String name, String password) {
		LOGGER.info(LOG_PREFIX_PORTAL + "UserEJB login:");

		if (name == null || password == null) {
			throw new IllegalArgumentException("Login no name or pw");
		}
		if (name.length() > USER_NAME_MAX_LENGTH || password.length() > PASSWORD_MAX_LENGTH) {
			throw new IllegalArgumentException("Login name or password too long");
		}
		PreparedStatement stmt = null;
		PreparedStatement insertStatement = null;
		Connection con = null;
		try {
			con = portalDatasource.getConnection();
			stmt = con.prepareStatement(USERNAME_QUERY);
			stmt.setString(1, name);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				String namee = rs.getString("name");
				String status = rs.getString("Status");
				String userId = rs.getString("id");
				String salt = rs.getString("salt");
				String secret = rs.getString("password");
				PasswordHash passwordHash = new PasswordHash();
				if (!passwordHash.verifyHash(password, salt, secret)) {
					throw new IllegalArgumentException("UserEJB password failure for:" + name);
				}
				boolean pwChange = rs.getBoolean("force_password_change");
				rs.close();
				LoginOutput user = new LoginOutput();
				user.setNickName(namee);
				user.setStatus(status);
				user.setForcePasswordChange(pwChange);
				insertStatement = con.prepareStatement(ACTIVE_LOGIN_INSERT);
				String activeLoginId = UUID.randomUUID().toString();
				insertStatement.setString(1, activeLoginId);
				insertStatement.setString(2, userId);
				insertStatement.setString(3, namee);
				insertStatement.setString(4, activeLoginId);
				int res = insertStatement.executeUpdate();
				if (res > 0) {
					user.setActiveLoginId(activeLoginId);
					LOGGER.info(LOG_PREFIX_PORTAL + "UserEJB returning user " + user);
					return user;
				} else {
					LOGGER.severe(LOG_PREFIX_PORTAL + "UserEJB login fail part2");
				}
			}
			LOGGER.info(LOG_PREFIX_PORTAL + "UserEJB login fail part3");
			return null;

		} catch (SQLException e) {
			LOGGER.severe(LOG_PREFIX_PORTAL + "UserEJB sqle" + e.getMessage());
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException e) {
				LOGGER.severe(LOG_PREFIX_PORTAL + "UserEJB finally crashed" + e.getMessage());
			}
		}
		throw new RuntimeException("UserEJB was not able to fetch name for " + name);
	}

	public LoginOutput register(String nickName, String password, String email) throws DuplicateKeyException {

		boolean isForbidden = com.tauhka.games.core.util.Constants.FORBIDDEN_WORD_PARTS.stream().anyMatch(nickName::equalsIgnoreCase);
		if (isForbidden) {
			throw new IllegalArgumentException("Registration nickname is forbidden:" + nickName);
		}
		Optional<String> forbiddenWordOptional = FORBIDDEN_WORD_PARTS.stream().filter(forbidden -> nickName.contains(forbidden)).findAny();
		System.out.println("GG");
		if (forbiddenWordOptional.isPresent()) {
			throw new IllegalArgumentException("Registration nickname contains forbidden word:" + forbiddenWordOptional.get() + " :" + nickName);
		}
		if (nickName == null || nickName.trim().length() < USER_NAME_MIN_LENGTH || nickName.trim().length() > USER_NAME_MAX_LENGTH) {
			throw new IllegalArgumentException("Registrating username does not match criteria:" + nickName);
		}
		if (password == null || password.length() < PASSWORD_MIN_LENGTH || password.length() > PASSWORD_MAX_LENGTH) {
			throw new IllegalArgumentException("Registrating password does not match criteria");
		}
		String nickNamea = nickName.trim();
		PreparedStatement stmt = null;
		Connection con = null;
		try {
			con = portalDatasource.getConnection();
			stmt = con.prepareStatement(REGISTER_INSERT);
			stmt.setString(1, nickNamea);
			stmt.setString(2, UUID.randomUUID().toString());
			stmt.setString(3, email);
			PasswordHash p = new PasswordHash();
			p.generateHashes(password);
			p.getPasswordHash();
			stmt.setString(4, p.getPasswordHash());
			stmt.setString(5, p.getSaltAsHex());
			int res = stmt.executeUpdate();
			if (res == 1) {
				LOGGER.info(LOG_PREFIX_PORTAL + "UserEJB registered");
				LoginOutput user = new LoginOutput();
				user.setNickName(nickNamea);
				return user;
			}
			LOGGER.severe(LOG_PREFIX_PORTAL + "UserEJB register fail part3");
			return null;

		} catch (SQLException e) {
			LOGGER.severe(LOG_PREFIX_PORTAL + "UserEJB register sqle" + e.getMessage() + e.getErrorCode());
			if (e.getSQLState().equals("23000")) {
				throw new DuplicateKeyException();
			}
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException e) {
				LOGGER.severe(LOG_PREFIX_PORTAL + "UserEJB register finally crashed" + e.getMessage());
			}
		}
		throw new RuntimeException("UserEJB runtimeException " + nickName);
	}

	// 3 different queries, TODO check optimizing possibilities, although max once
	// per cache-timeout called
	public TopLists getTopPlayers() {

		PreparedStatement stmt = null;
		Connection con = null;
		try {
			con = portalDatasource.getConnection();
			TopLists topLists = new TopLists();
			stmt = con.prepareStatement(TOP_TICTACTOE_PLAYERS_SQL);
			ResultSet rs = stmt.executeQuery();
			topLists.setFetchInstant(Instant.now());
			while (rs.next()) {
				TopPlayer t = new TopPlayer();
				t.setNickname(rs.getString("name"));
				t.setRankingTicTacToe((int) rs.getDouble("ranking_tictactoe"));
				topLists.addTicTacToePlayer(t);
			}
			rs.close();
			stmt.close();
			stmt = con.prepareStatement(TOP_CONNECT_FOUR_PLAYERS_SQL);
			ResultSet rs2 = stmt.executeQuery();

			while (rs2.next()) {
				TopPlayer t = new TopPlayer();
				t.setNickname(rs2.getString("name"));
				t.setRankingConnectFour((int) rs2.getDouble("ranking_connectfour"));
				topLists.addConnectFourPlayer(t);
			}
			rs2.close();

			stmt = con.prepareStatement(SELECT_GAME_COUNTS_SQL);
			ResultSet rs3 = stmt.executeQuery();
			if (rs3.next()) {
				int connectFours = rs3.getInt("connectfours");
				int tictactoes = rs3.getInt("tictactoes");
				int eightBalls = rs3.getInt("eightballs");
				topLists.setTotalConnectFours(connectFours);
				topLists.setTotalTictactoes(tictactoes);
				topLists.setTotalEightBalls(eightBalls);
			}
			rs3.close();
			stmt = con.prepareStatement(TOP_EIGHT_BALL_PLAYERS_SQL);
			ResultSet rs4 = stmt.executeQuery();
			while (rs4.next()) {
				TopPlayer t = new TopPlayer();
				t.setNickname(rs4.getString("name"));
				t.setRankingEightBall((int) rs4.getDouble("ranking_eightball"));
				t.setPlayedEightBalls(USER_NAME_MIN_LENGTH);
				topLists.addEightBallPlayer(t);
			}
			rs4.close();
			return topLists;

		} catch (SQLException e) {
			LOGGER.severe(LOG_PREFIX_PORTAL + "UserEJB register sqle" + e.getMessage() + e.getErrorCode());
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException e) {
				LOGGER.severe(LOG_PREFIX_PORTAL + "UserEJB register finally crashed" + e.getMessage());
			}
		}
		throw new RuntimeException("UserEJB topPlayers exception ");
	}

	public boolean logout(String activeLoginToken) {
		LOGGER.info(LOG_PREFIX_PORTAL + "UserEJB logout:" + activeLoginToken);
		PreparedStatement stmt = null;
		Connection con = null;
		if (activeLoginToken == null || activeLoginToken.startsWith(Constants.GUEST_LOGIN_TOKEN_START) || activeLoginToken.startsWith(NULL) || activeLoginToken.equals("")) {
			LOGGER.info(LOG_PREFIX_PORTAL + "UserEJB logout has no token or anonymous logout ");
			return false;
		}
		UUID token = UUID.fromString(activeLoginToken);
		try {
			con = portalDatasource.getConnection();
			stmt = con.prepareStatement(REMOVE_ACTIVE_LOGIN);
			stmt.setString(1, token.toString());
			int res = stmt.executeUpdate();
			if (res == 1) {
				LOGGER.info(LOG_PREFIX_PORTAL + "UserEJB deleted activeLogin");
				return true;
			}
			LOGGER.fine(LOG_PREFIX_PORTAL + "UserEJB logout fail part3:" + activeLoginToken);
			return false;

		} catch (SQLException e) {
			LOGGER.severe("UserEJB logout sqle" + e.getMessage() + e.getErrorCode());
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException e) {
				LOGGER.log(Level.SEVERE, LOG_PREFIX_PORTAL + "UserEJB logout finally crashed" + e);
			}
		}
		throw new RuntimeException(LOG_PREFIX_PORTAL + "UserEJB logout runtimeException:" + token);
	}
}
