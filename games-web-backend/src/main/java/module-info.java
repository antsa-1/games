module tauhka.games {
	exports com.tauhka.games.messaging.util;

	exports com.tauhka.games.ejb;
	exports com.tauhka.games.messaging.handlers;
	exports com.tauhka.games.web.filter;
	exports com.tauhka.games.messaging;
	exports com.tauhka.games.web.websocket;

	requires jakarta.annotation;
	requires jakarta.cdi;
	requires jakarta.ejb.api;
	requires jakarta.inject;
	requires jakarta.json;
	requires jakarta.json.bind;
	requires jakarta.servlet;
	requires jakarta.websocket;
	requires java.logging;
	requires java.sql;
	requires transitive java.desktop;
	requires transitive tauhka.core;
	requires transitive tauhka.pool;
}