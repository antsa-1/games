module tauhka.core {
	exports com.tauhka.games.core.ai;
	exports com.tauhka.games.core;
	exports com.tauhka.games.core.tables;
	exports com.tauhka.games.core.stats;
	exports com.tauhka.games.core.twodimen.ai;
	exports com.tauhka.games.core.twodimen.util;
	exports com.tauhka.games.core.util;
	exports com.tauhka.games.core.twodimen;

	requires jakarta.inject;
	requires jakarta.json;
	requires jakarta.json.bind;
	requires java.logging;
}