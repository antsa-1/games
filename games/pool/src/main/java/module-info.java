module tauhka.pool {
	exports com.tauhka.games.pool.eightball;
	exports com.tauhka.games.pool;
	//exports com.tauhka.games.pool.debug;

	requires jakarta.json.bind;
	requires java.desktop;
	requires java.logging;
	requires transitive tauhka.core;
}