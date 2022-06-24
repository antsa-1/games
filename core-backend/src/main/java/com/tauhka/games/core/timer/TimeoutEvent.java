package com.tauhka.games.core.timer;

import com.tauhka.games.core.tables.Table;

/**
 * @author antsa-1 from GitHub 24 Jun 2022
 **/

@IGameTimedOut
public class TimeoutEvent {

	private Table table;

	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	@Override
	public String toString() {
		return "TimeoutEvent [table=" + table + "]";
	}

}
