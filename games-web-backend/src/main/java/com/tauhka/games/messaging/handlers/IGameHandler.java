package com.tauhka.games.messaging.handlers;

import com.tauhka.games.core.Move;
import com.tauhka.games.core.Table;

public interface IGameHandler {

	public void addToken(Table table, Move move);
	
}
