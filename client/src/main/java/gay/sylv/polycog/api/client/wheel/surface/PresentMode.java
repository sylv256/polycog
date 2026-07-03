package gay.sylv.polycog.api.client.wheel.surface;

/// A platform-specific [Surface] setting indicating how images are to be
/// presented to the surface.
public enum PresentMode {
	IMMEDIATE,
	MAILBOX,
	FIFO,
	FIFO_RELAXED;

	/// As present modes cannot be specified, a preference may be given for
	/// which present mode should be selected.
	public enum Preference {
		/// No vertical sync
		NO_SYNC,
		/// Higher latency vertical sync, preferring a first in first out queue
		FIFO,
		/// Lowest latency vertical sync as supported
		LOW_LATENCY
	}
}
