module tauhka.portal {
	exports com.tauhka.portal.util;
	exports com.tauhka.portal.web.security;
	exports com.tauhka.portal.ejb;
	exports com.tauhka.portal.exeptions;
	exports com.tauhka.portal.highscore;
	exports com.tauhka.portal.login;
	exports com.tauhka.portal.web.filter;
	exports com.tauhka.portal.web.resources;

	requires jakarta.annotation;
	requires jakarta.ejb.api;
	requires jakarta.inject;
	requires jakarta.json.bind;
	requires java.logging;
	requires java.sql;
	requires transitive jakarta.servlet;
	requires transitive jakarta.ws.rs;
	requires jakarta.persistence;
	requires jakarta.cdi;
}