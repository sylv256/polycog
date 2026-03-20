package gay.sylv.polycog.impl.client.wheel.vulkan.window;

import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;

import gay.sylv.polycog.api.client.wheel.window.Surface;
import gay.sylv.polycog.impl.client.wheel.GameRenderer;
import gay.sylv.polycog.impl.client.wheel.NativeResource;
import gay.sylv.polycog.impl.client.wheel.vulkan.core.VkResult;

public final class VkSurface extends NativeResource<Long> implements Surface {
	private final VkSurfaceCapabilitiesKHR surfaceCapabilities =
			this.allocStruct(VkSurfaceCapabilitiesKHR::calloc);

	public VkSurface(VkWindow window) {
		long[] surfaceHandle = new long[1];
		GameRenderer.assertSuccess(VkResult.fromRaw(GLFWVulkan.glfwCreateWindowSurface(
						GameRenderer.getInstance().getVkInstance(),
						window.getVkHandle(),
						null,
						surfaceHandle
		)), "Failed to create window surface");
		this.vkHandle = surfaceHandle[0];
		GameRenderer.assertSuccess(VkResult.fromRaw(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(
				window.getPhysicalDevice().getVkHandle(),
				this.getVkHandle(),
				this.surfaceCapabilities
		)), "Failed to get surface capabilities");
	}

	public VkSurfaceCapabilitiesKHR getSurfaceCapabilities() {
		return surfaceCapabilities;
	}

	@Override
	protected void onFree() {
		KHRSurface.vkDestroySurfaceKHR(
				GameRenderer.getInstance().getVkInstance(),
				this.getVkHandle(),
				null
		);
	}
}
