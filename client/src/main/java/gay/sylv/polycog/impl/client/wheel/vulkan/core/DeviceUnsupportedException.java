package gay.sylv.polycog.impl.client.wheel.vulkan.core;

public class DeviceUnsupportedException extends IllegalStateException {
	public DeviceUnsupportedException() {
		this("");
	}

	public DeviceUnsupportedException(String reason) {
		String message = """
				Vulkan is not supported on your platform.
				This means your computer is either too old or not officially supported.
				Do not open support tickets or create issues; they will be closed.""";

		if (!reason.isEmpty()) {
			message += "\n\n" + reason;
		}

		super(message);
	}
}
