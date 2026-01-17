/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
*/

package gay.sylv.polycog.impl.client.bootstrap;

import gay.sylv.polycog.impl.bootstrap.MainCommon;
import gay.sylv.polycog.impl.bootstrap.sprocket.Sprocket;

public final class MainClient {
	static void main() {
		Sprocket.bootstrap(MainClient.class);
	}

	/// This is called after [Sprocket] loads Jar-in-Jar dependencies.
	@SuppressWarnings("unused") // used via reflection
	public static void postMain() {
		MainCommon.bootstrap();
		GameClient.initialize();
	}
}
