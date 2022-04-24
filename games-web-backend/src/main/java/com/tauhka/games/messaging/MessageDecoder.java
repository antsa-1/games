package com.tauhka.games.messaging;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.tauhka.games.core.twodimen.ai.TwoDimensionalBoardAdapter;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;

public class MessageDecoder implements Decoder.Text<Message> {

	private static final Logger LOGGER = Logger.getLogger(MessageDecoder.class.getName());
	private final JsonbConfig gamesConfig = new JsonbConfig().withAdapters(new TwoDimensionalBoardAdapter());
	private Jsonb jsonb = JsonbBuilder.create(gamesConfig);

	@Override
	public void destroy() {
		LOGGER.fine("MessageDecoder destroy");
	}

	@Override
	public Message decode(String jsonMessage) throws DecodeException {
		try {
			return jsonb.fromJson(jsonMessage, Message.class);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, " MessageDecoding failed, something unexpected arrived:", e);
		}
		throw new IllegalArgumentException("unexpected messagedecoding ");

	}

	@Override
	public boolean willDecode(String arg0) {
		// TODO Auto-generated method stub
		return arg0 != null && arg0.contains("title");
	}

}