package com.tauhka.games.messaging.handlers;

import java.util.Optional;
import java.util.stream.Stream;

import com.tauhka.games.core.tables.Table;
import com.tauhka.games.web.websocket.CommonEndpoint;

/**
 * @author antsa-1 from GitHub 12 May 2022
 **/

public class CommonHandler {

	protected Table findUserTable(CommonEndpoint endpoint) {
		Stream<Table> stream = CommonEndpoint.TABLES.values().stream();
		stream = stream.filter(table -> table.isPlayer(endpoint.getUser()));
		Optional<Table> tableOptional = stream.findFirst();
		if (tableOptional.isEmpty()) {
			throw new IllegalArgumentException("no table for:" + endpoint.getUser());
		}
		return tableOptional.get();
	}
}
