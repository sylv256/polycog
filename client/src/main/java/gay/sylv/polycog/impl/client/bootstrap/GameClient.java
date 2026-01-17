/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
*/

package gay.sylv.polycog.impl.client.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GameClient {
	public final Logger logger;
	private static GameClient INSTANCE;

	private GameClient() {
		logger = LoggerFactory.getLogger("PolyCog/Client");
		logger.info("Hello, Client!");
	}

	public static void initialize() {
		if (INSTANCE != null) {
			throw new IllegalStateException("GameClient is already initialized");
		}

		INSTANCE = new GameClient();
	}

	public static GameClient getInstance() {
		return INSTANCE;
	}
}
