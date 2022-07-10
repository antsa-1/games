module tauhka.core {
	exports com.tauhka.games.core.ai;
	exports com.tauhka.games.core;
	exports com.tauhka.games.core.tables;
	exports com.tauhka.games.core.stats;
	exports com.tauhka.games.core.events;
	exports com.tauhka.games.core.twodimen.ai;
	exports com.tauhka.games.core.twodimen.util;
	exports com.tauhka.games.core.timer;
	exports com.tauhka.games.core.util;
	exports com.tauhka.games.core.twodimen;

	requires jakarta.inject;
	requires jakarta.json;
	requires transitive jakarta.websocket;
	requires java.logging;
	requires jakarta.json.bind;
}