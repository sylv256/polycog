package gay.sylv.polycog.impl.client.wheel.vulkan.window;

import static org.lwjgl.glfw.GLFW.GLFW_CLIENT_API;
import static org.lwjgl.glfw.GLFW.GLFW_NO_API;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;

import gay.sylv.polycog.api.client.wheel.window.Surface;
import gay.sylv.polycog.api.client.wheel.window.Window;
import gay.sylv.polycog.impl.client.wheel.NativeResource;
import gay.sylv.polycog.impl.client.wheel.vulkan.device.VkPhysicalGpuDevice;

public final class VkWindow extends NativeResource<Long> implements Window {
	private final VkPhysicalGpuDevice physicalDevice;
	private final VkSurface surface;

	public VkWindow(
			VkPhysicalGpuDevice physicalDevice,
			int width,
			int height,
			String name
	) {
		glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
		this.vkHandle = glfwCreateWindow(
				width,
				height,
				name,
				0,
				0
		);

		if (this.vkHandle == 0) {
			throw new IllegalStateException("Failed to initialize window");
		}

		this.physicalDevice = physicalDevice;
		this.surface = this.addChild(new VkSurface(this));
	}

	@Override
	public Surface getSurface() {
		return surface;
	}

	public VkPhysicalGpuDevice getPhysicalDevice() {
		return physicalDevice;
	}

	@Override
	protected void onFree() {
		glfwDestroyWindow(this.getVkHandle());
	}
}
