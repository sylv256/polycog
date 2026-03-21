/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.impl.core;

import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GameCommon {
	public final Logger logger;
	private static @Nullable GameCommon INSTANCE;

	private GameCommon() {
		this.logger = LoggerFactory.getLogger("Polycog");
		this.logger.info("Common thread initialized");
	}

	public static void initialize() {
		if (INSTANCE != null) {
			throw new IllegalStateException("GameCommon is already initialized");
		}

		INSTANCE = new GameCommon();
	}

	public static GameCommon getInstance() {
		return Objects.requireNonNull(INSTANCE);
	}
}
