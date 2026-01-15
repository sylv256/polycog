/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
*/

package gay.sylv.polycog.impl.client.bootstrap;

@SuppressWarnings("InstantiationOfUtilityClass")
public final class GameClient {
	private static GameClient INSTANCE;

	private GameClient() {
		IO.println("Hello, Client!");
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
