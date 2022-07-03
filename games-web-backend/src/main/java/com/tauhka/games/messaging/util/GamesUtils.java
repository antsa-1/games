package com.tauhka.games.messaging.util;

import static com.tauhka.games.core.util.Constants.GUEST_LOGIN_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.tauhka.games.core.Game;
import com.tauhka.games.core.GameMode;
import com.tauhka.games.core.User;
import com.tauhka.games.core.ai.ArtificialUser;
import com.tauhka.games.core.stats.GameStatisticsEvent;
import com.tauhka.games.messaging.Message;

/**
 * @author antsa-1 from GitHub 25 Mar 2022
 **/

public class GamesUtils {
// Collects game into a list once per application restart
	public static final List<Game> games;
	static {
		games = new ArrayList<Game>();
		Game tictactoe = new Game();
		tictactoe.setGameId(GameMode.TIC_TAC_TOE);
		tictactoe.setName("TicTacToe");
		tictactoe.setGameModes(GameMode.getGamemodes(GameMode.TIC_TAC_TOE));

		Game connectFour = new Game();
		connectFour.setGameId(GameMode.CONNECT4);
		connectFour.setName("ConnectFour");
		connectFour.setGameModes(GameMode.getGamemodes(GameMode.CONNECT4));

		Game pool = new Game();
		pool.setGameId(GameMode.POOL);
		pool.setName("Pool");
		pool.setGameModes(GameMode.getGamemodes(GameMode.POOL));

		Game yatzy = new Game();
		yatzy.setGameId(GameMode.YATZY);
		yatzy.setName("Yatzy");
		yatzy.setGameModes(GameMode.getGamemodes(GameMode.POOL));
		games.add(tictactoe);
		games.add(connectFour);
		games.add(pool);
		games.add(yatzy);
	}

	public static boolean isBothPlayersLoggedIn(GameStatisticsEvent gameStats) {
		User playerA = gameStats.getGameResult().getPlayerA();
		User playerB = gameStats.getGameResult().getPlayerB();
		boolean computerPlayer = playerB instanceof ArtificialUser;
		// Computer sits always on playerB position
		return !playerA.getName().startsWith(GUEST_LOGIN_NAME) && !playerB.getName().startsWith(GUEST_LOGIN_NAME) && !computerPlayer;
	}

	public static boolean isOneRegisteredPlayer(GameStatisticsEvent gameStats) {
		User playerA = gameStats.getGameResult().getPlayerA();
		User playerB = gameStats.getGameResult().getPlayerB();
		boolean computerPlayer = playerB instanceof ArtificialUser;
		// Computer sits always on playerB position
		return !playerA.getName().startsWith(GUEST_LOGIN_NAME) || !playerB.getName().startsWith(GUEST_LOGIN_NAME) && !computerPlayer;
	}

	public static UUID validateTableId(Message message) {
		if (message.getMessage() == null) {
			throw new IllegalArgumentException("TableParam missing");
		}
		String idFromUser = message.getMessage();
		if (idFromUser == null) {
			throw new IllegalArgumentException("Id is missing");
		}
		UUID idFromComponent = UUID.fromString(idFromUser);
		if (idFromComponent.toString().equals(idFromUser)) {
			return idFromComponent;
		}
		throw new IllegalArgumentException("TableId does not match with componentId:");
	}
}
