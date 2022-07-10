package com.tauhka.games.core.timer;

import java.util.TimerTask;

import com.tauhka.games.core.User;
import com.tauhka.games.core.tables.Table;

/**
 * @author antsa-1 from GitHub 12 May 2022
 **/

public class ReduceTimeTask extends TimerTask {
	private Table table;
	private User userRunningOutOfTime;

	public ReduceTimeTask(Table table, User user) {
		this.table = table;
		this.userRunningOutOfTime = user;
	}

	@Override
	public void run() {
		int seconds = table.getSecondsLeft();
		seconds--;
		table.setSecondsLeft(seconds);
		// System.out.println("Seconds LEFT:" + table.getSecondsLeft());
		if (table.getSecondsLeft() <= 0) {
			table.onTimeout(userRunningOutOfTime);
		}
	}
}
