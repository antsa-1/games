package com.tauhka.portal.ejb;

import static com.tauhka.games.core.util.Constants.LOG_PREFIX_PORTAL;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ejb.Stateless;

@Stateless(name = "FeedbackEJB")
public class FeedbackEJB {
	private static final String ENVIRONMENT = System.getProperty("Server_Environment");
	// Developer machine path windows system
	private final String fileNameLocal = "C:\\Users\\path-to\\projects\\TicTacToe\\BackEndJava\\feedbak.txt";
	// Prod machine path with Linux
	private final String fileNameProd = "/opt/apache-tomee-plume-9.0.0-M8/feedbak/feedbak.txt"; 
	private static final Logger LOGGER = Logger.getLogger(FeedbackEJB.class.getName());
	private static final String ddMMyyyy = "dd.MM.yyyy HH:mm:ss";

	public void writeFeedBack(String feedback) throws Exception {
		LOGGER.entering(FeedbackEJB.class.getName(), "writeFeedBack");
		Path path = null;
		if (ENVIRONMENT.equalsIgnoreCase(com.tauhka.games.core.util.Constants.ENVIRONMENT_PRODUCTION)) {
			path = Paths.get(fileNameProd);
		} else {
			path = Paths.get(fileNameLocal);
		}
		long bytes = Files.size(path);
		if (bytes > 104857600) { // 100 MB max
			throw new RuntimeException("Feedback fileSize exceeded");
		}
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
				StandardOpenOption.APPEND)) {
			writer.write("\n***** ");
			DateFormat formatter = new SimpleDateFormat(ddMMyyyy);
			String today = formatter.format(new Date());
			writer.write(today + " *****\n");
			writer.write(feedback);
			writer.close();

		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, LOG_PREFIX_PORTAL + "Feedback was not successful", e);
			throw e;
		}
	}

}