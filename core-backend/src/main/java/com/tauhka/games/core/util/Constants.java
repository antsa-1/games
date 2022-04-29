package com.tauhka.games.core.util;

import java.util.ArrayList;
import java.util.List;

public final class Constants {
	public static final int USER_NAME_MIN_LENGTH = 1;
	public static final int USER_NAME_MAX_LENGTH = 15;
	public static final int PASSWORD_MIN_LENGTH = 8;
	public static final int PASSWORD_MAX_LENGTH = 150;
	public static final int CHAT_MESSAGE_MAX_LENGTH = 75;
	public static final int CHAT_MESSAGE_MIN_LENGTH = 1;
	public static final String GUEST_LOGIN_NAME = "Guest_";
	public static final String GUEST_LOGIN_TOKEN_START = "Guest:";
	public static final String OLAV_COMPUTER = "Olav_computer";
	public static final double OLAV_COMPUTER_TICTACTOE_RANKING = 1200;
	public static final double OLAV_COMPUTER_CONNECT_FOUR_RANKING = 1205;
	public static final double OLAV_COMPUTER_EIGHT_BALL_RANKING = 1299;
	public static final String OLAV_COMPUTER_ID = "123e4567-e89b-12d3-a456-426652340000";
	public static final String ENVIRONMENT_PRODUCTION = "PRODUCTION";
	public static final String ENVIRONMENT_DEVELOPMENT = "DEVELOPMENT";
	public static final String WEBSOCKET_PRODUCTION_ORIGIN = "http://35.217.7.146"; // TODO replace with real prod. URL
	public static final String WEBSOCKET_LOCALHOST_ORIGIN = "http://localhost:8080"; //
	public static final String SYSTEM = "System";
	public static final String NULL = "null";
	public static final String LOG_PREFIX = "TAUHKA_";
	public static final String LOG_PREFIX_PORTAL = LOG_PREFIX + "PORTAL ";
	public static final String LOG_PREFIX_GAMES = LOG_PREFIX + "GAMES ";

	public static final List<String> FORBIDDEN_WORD_PARTS = new ArrayList<String>();
	static {
		FORBIDDEN_WORD_PARTS.add("http");
		FORBIDDEN_WORD_PARTS.add("@");
		FORBIDDEN_WORD_PARTS.add("https");
		FORBIDDEN_WORD_PARTS.add("www");
		FORBIDDEN_WORD_PARTS.add("system");
		FORBIDDEN_WORD_PARTS.add("System");
		FORBIDDEN_WORD_PARTS.add(Constants.GUEST_LOGIN_NAME);
		FORBIDDEN_WORD_PARTS.add(Constants.GUEST_LOGIN_TOKEN_START);
		FORBIDDEN_WORD_PARTS.add(OLAV_COMPUTER);
		FORBIDDEN_WORD_PARTS.add("null");
		FORBIDDEN_WORD_PARTS.add(".com");
		FORBIDDEN_WORD_PARTS.add("/");
		FORBIDDEN_WORD_PARTS.add("//");
		FORBIDDEN_WORD_PARTS.add("///");
		FORBIDDEN_WORD_PARTS.add("\\");
		FORBIDDEN_WORD_PARTS.add(":");
		FORBIDDEN_WORD_PARTS.add("<");
		FORBIDDEN_WORD_PARTS.add("&lt;");
	}
}