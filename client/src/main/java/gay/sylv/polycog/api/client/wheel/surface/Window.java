package gay.sylv.polycog.api.client.wheel.surface;

import gay.sylv.polycog.api.client.wheel.device.GpuDevice;
import gay.sylv.polycog.impl.client.wheel.RenderObject;
import gay.sylv.polycog.impl.client.wheel.vulkan.window.VkWindow;

public sealed interface Window extends AutoCloseable, RenderObject<Window> permits VkWindow {
	static Window of(
			GpuDevice device,
			int width,
			int height,
			String name
	) {
		return new VkWindow(device.wheel$impl(), width, height, name);
	}

	int getWidth();

	int getHeight();

	void setResizable(boolean resizable);

	boolean isResizable();

	Surface getSurface();
}
