module tauhka.core {
	exports com.tauhka.games.core;
	exports com.tauhka.games.core.stats;
	exports com.tauhka.games.core.twodimen.util;
	exports com.tauhka.games.core.util;
	exports com.tauhka.games.core.twodimen;

	requires jakarta.json;
	requires jakarta.json.bind;
	requires java.logging;
	requires jakarta.inject;
}