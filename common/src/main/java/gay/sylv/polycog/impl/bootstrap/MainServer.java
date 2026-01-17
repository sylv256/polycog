/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
*/

package gay.sylv.polycog.impl.bootstrap;

import gay.sylv.polycog.impl.bootstrap.sprocket.Sprocket;

public final class MainServer {
	static void main() {
		Sprocket.bootstrap(MainServer.class);
	}

	/// This is called after [Sprocket] loads Jar-in-Jar dependencies.
	@SuppressWarnings("unused") // used via reflection
	public static void postMain() {
		IO.println("Hello, Server!");
		MainCommon.bootstrap();
	}
}
