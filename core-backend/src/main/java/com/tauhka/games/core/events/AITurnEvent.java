package com.tauhka.games.core.events;

import com.tauhka.games.core.tables.Table;

/**
 * @author antsa-1 from GitHub 9 Jul 2022
 **/

public class AITurnEvent {

	private Table table;
	private int delay;

	public AITurnEvent(Table table, int delay) {
		super();
		this.table = table;
		this.delay = delay;
	}

	public Table getTable() {
		return table;
	}

	public int getDelay() {
		return delay;
	}

	public void setTable(Table table) {
		this.table = table;
	}
}
