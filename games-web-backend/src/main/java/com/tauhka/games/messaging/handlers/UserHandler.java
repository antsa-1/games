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
import com.tauhka.games.ejb.UserEJBA;
import com.tauhka.games.messaging.Message;
import com.tauhka.games.messaging.MessageTitle;
import com.tauhka.games.web.websocket.CloseWebSocketException;
import com.tauhka.games.web.websocket.CommonEndpoint;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.websocket.Session;

//@Default
@Dependent
public class UserHandler {
	private static final Logger LOGGER = Logger.getLogger(UserHandler.class.getName());
	private static int anonymCount = 0;
	@EJB // 11.02.2022 Using @Inject did not seem to work in prod-server even though changed beans.xml location META-INF<-->WEB-INF -> Changed to @EJB
	private UserEJBA userEJBa;

	public Message handleLogin(Message message, Session session, CommonEndpoint endpoint) {
		String name = null;
		Message loginMessage = new Message();
		try {
			if (message.getMessage() == null || message.getMessage().equals(NULL) || message.getMessage().trim().length() < 1 || message.getMessage().startsWith(ANONYM_LOGIN_TOKEN_START)) {
				name = ANONYM_LOGIN_NAME_START + updateAnonymCount();
				loginMessage.setToken(ANONYM_LOGIN_TOKEN_START + UUID.randomUUID().toString());
			} else {
				UUID userId = UUID.fromString(message.getMessage()); // security check
				name = userEJBa.verifyWebsocketToken(userId.toString());
				if (name == null || name.trim().length() < 1) {
					throw new CloseWebSocketException("Name was not found from logins for:" + userId);
				}
				loginMessage.setToken(userId.toString());
			}
			// CleanUp ghost tables
			Stream<Table> stream = CommonEndpoint.TABLES.values().stream();
			List<Table> tables = stream.filter(table -> table.getPlayerA() == null).collect(Collectors.toList());
			for (Table t : tables) {
				LOGGER.fine("Siivotaan pöytä:" + t);
				CommonEndpoint.TABLES.remove(t.getId());
			}
			User user = new User(name);
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
