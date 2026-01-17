/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.impl.client.bootstrap;

import gay.sylv.polycog.impl.bootstrap.MainCommon;
import gay.sylv.polycog.impl.bootstrap.sprocket.Sprocket;
import gay.sylv.polycog.impl.client.core.GameClient;

public final class MainClient {
	static void main() {
		Sprocket.bootstrap(MainClient.class);
	}

	/// This is called after [Sprocket] loads Jar-in-Jar dependencies.
	@SuppressWarnings("unused") // used via reflection
	public static void postMain() {
		MainCommon.bootstrap();
		GameClient.createInstance();
		// Don't do anything past this point as we're not on the Client Thread
	}
}
