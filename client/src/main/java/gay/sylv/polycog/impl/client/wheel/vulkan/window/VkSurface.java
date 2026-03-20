package gay.sylv.polycog.impl.client.wheel.vulkan.window;

import static gay.sylv.polycog.impl.client.core.GameClient.handleErrorSDL;
import static org.lwjgl.sdl.SDLVulkan.SDL_Vulkan_CreateSurface;
import static org.lwjgl.sdl.SDLVulkan.SDL_Vulkan_DestroySurface;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;

import gay.sylv.polycog.api.client.wheel.window.Surface;
import gay.sylv.polycog.impl.client.wheel.GameRenderer;
import gay.sylv.polycog.impl.client.wheel.NativeResource;
import gay.sylv.polycog.impl.client.wheel.vulkan.core.DeviceUnsupportedException;
import gay.sylv.polycog.impl.client.wheel.vulkan.core.VkResult;

public final class VkSurface extends NativeResource<LongBuffer> implements Surface {
	private final VkSurfaceCapabilitiesKHR surfaceCapabilities =
			this.allocStruct(VkSurfaceCapabilitiesKHR::calloc);

	public VkSurface(VkWindow window) {
		this.vkHandle = this.mallocLongs(1);

		if (!SDL_Vulkan_CreateSurface(
				window.getVkHandle(),
				GameRenderer.getInstance().getVkInstance(),
				null,
				this.vkHandle
		)) {
			throw handleErrorSDL(new DeviceUnsupportedException("Vulkan surfaces are unsupported"));
		}

		GameRenderer.assertSuccess(VkResult.fromRaw(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(
				window.getPhysicalDevice().getVkHandle(),
				this.getVkHandle().get(0),
				this.surfaceCapabilities
		)), "Failed to get surface capabilities");
	}

	public VkSurfaceCapabilitiesKHR getSurfaceCapabilities() {
		return surfaceCapabilities;
	}

	@Override
	protected void onFree() {
		SDL_Vulkan_DestroySurface(
				GameRenderer.getInstance().getVkInstance(),
				this.getVkHandle().get(0),
				null
		);
	}
}
