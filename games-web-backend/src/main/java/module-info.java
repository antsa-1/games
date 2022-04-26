module tauhka.games {
	exports com.tauhka.games.messaging.util;
	//exports com.tauhka.tictactoe.tests.win;
	exports com.tauhka.games.ejb;
	//exports com.tauhka.connectfour.tests.board;
	exports com.tauhka.games.messaging.handlers;
	exports com.tauhka.games.web.filter;
	exports com.tauhka.games.messaging;
	exports com.tauhka.games.web.websocket;
	//exports com.tauhka.tictactoe.tests.evaluation;

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
	requires tauhka.core;
	requires tauhka.pool;
	//requires org.junit.jupiter.api;  TODO next time
}