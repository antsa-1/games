package com.tauhka.games.yatzy;

import java.util.TimerTask;

/**
 * @author antsa-1 from GitHub 24 Jun 2022
 **/

public class ReduceTimeTask extends TimerTask {
	private int secondsLeft;
	private YatzyTable table;

	public ReduceTimeTask(YatzyTable table) {
		// this.secondsLeft = table.getTimeControlIndex();
		this.secondsLeft = 20; // TODO use real timecontrol
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
