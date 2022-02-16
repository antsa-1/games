package com.tauhka.games.ejb;

import java.util.logging.Logger;

import javax.sql.DataSource;

import com.tauhka.games.core.stats.GameStatisticsEvent;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.enterprise.event.ObservesAsync;

@Stateless
public class StatisticsEJB { // To core package?!?!?

	private static final Logger LOGGER = Logger.getLogger(StatisticsEJB.class.getName());
	@Resource(name = "jdbc/MariaDb")
	private DataSource gamesDataSource;

	public void observeGameResult(@ObservesAsync GameStatisticsEvent scoreEvent) {
		// If Anonym player then do not update..
		LOGGER.info("StatisticsEJB should update now database with " + scoreEvent);

	}
}
