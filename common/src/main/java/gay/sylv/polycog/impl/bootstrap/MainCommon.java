/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.impl.bootstrap;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import gay.sylv.polycog.impl.core.GameCommon;
import gay.sylv.polycog.impl.share.Constants;

public final class MainCommon {
	public static void bootstrap() {
		if (Constants.IS_DEBUG) {
			Configurator.setRootLevel(Level.TRACE);
		} else {
			Configurator.setRootLevel(Level.INFO);
		}

		GameCommon.initialize();
	}
}
