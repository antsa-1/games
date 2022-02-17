package com.tauhka.games.web.websocket;

import com.tauhka.games.core.util.Constants;

import jakarta.websocket.server.ServerEndpointConfig;

public class CommonEndpointConfiguration extends ServerEndpointConfig.Configurator {
	private static final String ENVIRONMENT = System.getProperty("Server_Environment");

	// Checks that browser sends origin header and it corresponds what is expected
	// to be received
	// Also checks that Server has been given Environment parameter for example.
	// -DServer_Environment="Development"
	// OR -DServer_Environment="Production"
	@Override
	public boolean checkOrigin(String originHeaderValue) {
		if (ENVIRONMENT == null) {
			throw new IllegalArgumentException("Environment is missing");
		}
		return true;
	}
}
