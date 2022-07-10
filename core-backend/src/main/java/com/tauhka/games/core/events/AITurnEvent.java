package com.tauhka.games.core.events;

import com.tauhka.games.core.tables.Table;

/**
 * @author antsa-1 from GitHub 9 Jul 2022
 **/

public class AITurnEvent {

	private Table table;

	public AITurnEvent(Table table) {
		super();
		this.table = table;
	}

	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		this.table = table;
	}
}
