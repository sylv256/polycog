package gay.sylv.polycog.impl.client.wheel.vulkan.window;

import static gay.sylv.polycog.impl.client.core.GameClient.handleErrorSDL;
import static org.lwjgl.sdl.SDLVideo.SDL_CreateWindow;
import static org.lwjgl.sdl.SDLVideo.SDL_DestroyWindow;
import static org.lwjgl.sdl.SDLVideo.SDL_WINDOW_VULKAN;

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
		this.vkHandle = SDL_CreateWindow(
				name,
				width,
				height,
				SDL_WINDOW_VULKAN
		);

		if (this.vkHandle == 0) {
			throw handleErrorSDL("Failed to initialize window");
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
		SDL_DestroyWindow(this.getVkHandle());
	}
}
