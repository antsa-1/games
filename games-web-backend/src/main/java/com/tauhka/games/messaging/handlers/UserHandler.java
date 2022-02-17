package com.tauhka.games.messaging.handlers;

import static com.tauhka.games.core.util.Constants.ANONYM_LOGIN_NAME_START;
import static com.tauhka.games.core.util.Constants.ANONYM_LOGIN_TOKEN_START;
import static com.tauhka.games.core.util.Constants.NULL;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.Table;
import com.tauhka.games.core.User;
import com.tauhka.games.ejb.UserEJBC;
import com.tauhka.games.messaging.Message;
import com.tauhka.games.messaging.MessageTitle;
import com.tauhka.games.web.websocket.CloseWebSocketException;
import com.tauhka.games.web.websocket.CommonEndpoint;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.websocket.Session;

//@Default
@Dependent
public class UserHandler {
	private static final Logger LOGGER = Logger.getLogger(UserHandler.class.getName());
	private static int anonymCount = 0;
	@Inject
	private UserEJBC userEJB;

	public Message handleLogin(Message message, Session session, CommonEndpoint endpoint) {
		cleanUpGhostTables();
		String name = null;
		User user = null;
		Message loginMessage = new Message();
		try {
			if (message.getMessage() == null || message.getMessage().equals(NULL) || message.getMessage().trim().length() < 1 || message.getMessage().startsWith(ANONYM_LOGIN_TOKEN_START)) {
				name = ANONYM_LOGIN_NAME_START + updateAnonymCount();
				user = new User(name);
				loginMessage.setToken(ANONYM_LOGIN_TOKEN_START + UUID.randomUUID().toString());
			} else {
				UUID activeLoginToken = UUID.fromString(message.getMessage());
				user = userEJB.verifyWebsocketToken(activeLoginToken.toString());

				if (user.getName().trim().length() < 1) {
					throw new CloseWebSocketException("Name was not found from logins for:" + activeLoginToken);
				}
				loginMessage.setToken(activeLoginToken.toString());
			}
			List<Table> tables;

			if (CommonEndpoint.ENDPOINTS.containsKey(user)) {
				// Registered users could replace existing one? But for now
				throw new CloseWebSocketException("Endpoints has already user:" + user);
			}
			CommonEndpoint.ENDPOINTS.put(user, endpoint);
			List<User> users = new ArrayList<User>(CommonEndpoint.ENDPOINTS.keySet());
			loginMessage.setUsers(users);
			tables = CommonEndpoint.TABLES.values().stream().collect(Collectors.toList());
			loginMessage.setTables(tables);
			loginMessage.setMessage(name);
			loginMessage.setGameModes(GameMode.getGameModes());
			loginMessage.setWho(user);
			loginMessage.setTitle(MessageTitle.LOGIN);
			return loginMessage;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "LoginHandler common excpetion", e);
			throw new CloseWebSocketException("LoginHandler exception");
		}
	}

	private void cleanUpGhostTables() {
		// If table without playerA exist -> clean
		Stream<Table> stream = CommonEndpoint.TABLES.values().stream();
		List<Table> tables = stream.filter(table -> table.getPlayerA() == null).collect(Collectors.toList());
		for (Table t : tables) {
			LOGGER.fine("Cleaning table" + t);
			CommonEndpoint.TABLES.remove(t.getId());
		}
	}

	private static synchronized int updateAnonymCount() {
		anonymCount++;
		return anonymCount;
	}

	public Message createNewPlayerMessage(User user) {
		Message newPlayerMessage = new Message();
		newPlayerMessage.setTitle(MessageTitle.NEW_PLAYER);
		newPlayerMessage.setWho(user);
		return newPlayerMessage;
	}

	public Message getUserDisconnectedMessage(CommonEndpoint CommonEndpoint, Table table) {
		Message removePlayerMessage = new Message();
		removePlayerMessage.setTitle(MessageTitle.REMOVE_PLAYER);
		removePlayerMessage.setWho(CommonEndpoint.getUser());
		if (table != null) {
			removePlayerMessage.setTable(table);
		}
		return removePlayerMessage;
	}

}
