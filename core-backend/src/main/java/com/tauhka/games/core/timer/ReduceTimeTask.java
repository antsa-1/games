package com.tauhka.games.core.timer;

import java.util.TimerTask;

import com.tauhka.games.core.tables.Table;

/**
 * @author antsa-1 from GitHub 12 May 2022
 **/

public class ReduceTimeTask extends TimerTask {
	private Table table;

	public ReduceTimeTask(Table table) {
		this.table = table;
	}

	@Override
	public void run() {
		int seconds = table.getSecondsLeft();
		seconds--;
		table.setSecondsLeft(seconds);
		System.out.println("Seconds LEFT:" + table.getSecondsLeft());
		if (table.getSecondsLeft() <= 0) {
			table.onTimeout();
		}
	}
}
