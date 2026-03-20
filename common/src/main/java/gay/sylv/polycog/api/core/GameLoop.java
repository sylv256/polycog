/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.api.core;

import java.io.Closeable;

public interface GameLoop extends Runnable, Closeable {
	/// Game logic run on a loop in the form of a [Runnable].
	/// @return Whether this loop should continue or exit.
	Control runLoop();

	/// Ran once upon calling [#run()].
	default void initialize() {
	}

	@Override
	default void close() {
	}

	/// Ran once after execution has concluded.
	default void endLoop() {
	}

	@Override
	default void run() {
		this.initialize();

		lo: while (true) {
			switch (this.runLoop()) {
				case CONTINUE -> {
				}
				case BREAK -> {
					break lo;
				}
			}
		}

		this.endLoop();
		this.close();
	}

	enum Control {
		CONTINUE,
		BREAK
	}
}
