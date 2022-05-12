package com.tauhka.games.core.timer;

import java.util.TimerTask;

/**
 * @author antsa-1 from GitHub 12 May 2022
 **/

//Testing timerTask for serverside time reduction
public class ReduceTimerTask extends TimerTask {
	private Integer timeLeft;

	public ReduceTimerTask(Integer timeLeft) {
		if (timeLeft == null) {
			throw new IllegalArgumentException("Timer startTime cannot be null");
		}
		this.timeLeft = timeLeft;
	}

	@Override
	public void run() {
		timeLeft--;
		if (timeLeft <= 0) {
			cancel();
		}
	}

}
