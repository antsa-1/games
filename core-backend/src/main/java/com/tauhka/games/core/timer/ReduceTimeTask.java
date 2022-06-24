package com.tauhka.games.core.timer;

import java.util.TimerTask;

import com.tauhka.games.core.tables.Table;

/**
 * @author antsa-1 from GitHub 12 May 2022
 **/

//Testing timerTask for serverside time reduction
public class ReduceTimeTask extends TimerTask {
	private int secondsLeft;
	private Table table;

	public ReduceTimeTask(Table table) {
		this.secondsLeft = 10; // table.getTimeControlIndex();
		this.table = table;
	}

	@Override
	public void run() {
		secondsLeft--;
		System.out.println("SECONDS LEFT:" + secondsLeft);
		if (secondsLeft <= 0) {
			table.onTimeout();
		}
	}

}
