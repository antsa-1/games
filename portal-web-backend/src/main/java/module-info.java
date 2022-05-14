module tauhka.portal {
	exports com.tauhka.portal.profile;
	exports com.tauhka.portal.util;
	exports com.tauhka.portal.web.security;
	exports com.tauhka.portal.ejb;
	exports com.tauhka.portal.cache;
	exports com.tauhka.portal.web.filter;
	exports com.tauhka.portal.login;
	exports com.tauhka.portal.exceptions;
	exports com.tauhka.portal.highscore;
	exports com.tauhka.portal.web.resources;

	requires jakarta.annotation;
	requires jakarta.cdi;
	requires jakarta.ejb.api;
	requires jakarta.inject;
	requires jakarta.json.bind;
	requires jakarta.persistence;
	requires jakarta.servlet;
	requires jakarta.transaction;
	requires transitive java.sql;
	requires transitive jakarta.ws.rs;
	requires transitive tauhka.core;

}