package gay.sylv.polycog.impl.core;

import gay.sylv.polycog.api.core.GameLoop;

public final class ServerGameLoop implements GameLoop {
	@Override
	public Control runLoop() {
		return Control.BREAK;
	}
}
