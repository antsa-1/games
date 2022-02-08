module tauhka.common {
	exports com.tauhka.common.web;
	exports com.tauhka.common.messaging;
	exports com.tauhka.common.messaging.handlers;
	exports com.tauhka.common.ejb;
	exports com.tauhka.common.web.filter;

	requires jakarta.annotation;
	requires jakarta.cdi;
	requires jakarta.ejb.api;
	requires jakarta.inject;
	requires jakarta.json.bind;
	requires jakarta.servlet;
	requires jakarta.websocket;
	requires java.logging;
	requires java.sql;
	requires tauhka.core;
}