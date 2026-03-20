package gay.sylv.polycog.api.client.wheel.window;

import gay.sylv.polycog.api.client.wheel.device.PhysicalDevice;
import gay.sylv.polycog.impl.client.wheel.RenderObject;
import gay.sylv.polycog.impl.client.wheel.vulkan.window.VkWindow;

public sealed interface Window extends AutoCloseable, RenderObject<Window> permits VkWindow {
	static Window of(
			PhysicalDevice physicalDevice,
			int width,
			int height,
			String name
	) {
		return new VkWindow(physicalDevice.wheel$impl(), width, height, name);
	}

	Surface getSurface();
}
