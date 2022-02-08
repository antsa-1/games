package com.tauhka.games.messaging.handlers;

import com.tauhka.games.core.GameToken;
import com.tauhka.games.core.Move;
import com.tauhka.games.core.Table;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Default;

@Default
@Dependent
public class ConnectFourGameHandler implements IGameHandler {

	@Override
	public void addToken(Table table, Move move) {
		int x = move.getX();
		int y = move.getY();
		GameToken token = table.getBoard()[x][y];
		if (token != null) {
			throw new IllegalArgumentException("Board already has token x:" + x + "+ y:" + y + " _" + this);
		}
		token = table.getPlayerInTurn().getGameToken();
		table.getBoard()[x][y] = token;

	}
}