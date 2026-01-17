/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.impl.share;

public final class Constants {
	public static final String NAME = "Polycog";
	public static final String ID = "polycog";

	// Flags
	public static final boolean IS_RUNNING_IDE = flag("debug.ide");
	public static final boolean WHEEL_GFX_VALIDATION = debugFlag("wheel.validation");

	private static boolean flag(String name) {
		return "1".equals(System.getProperty(ID + "." + name));
	}

	private static boolean devFlag(String name) {
		return IS_RUNNING_IDE || flag(name);
	}

	private static boolean debugFlag(String name) {
		return devFlag("debug." + name);
	}
}
