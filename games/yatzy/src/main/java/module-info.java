module tauhka.yatzy {
	exports com.tauhka.games.yatzy;

	requires jakarta.cdi;
	requires transitive jakarta.inject;
	requires java.logging;
	requires transitive tauhka.core;
	requires jakarta.json.bind;
	requires transitive java.sql;
	requires transitive jakarta.json;
	requires cdi.api;
	requires java.naming;
	requires jakarta.ejb.api;
}
