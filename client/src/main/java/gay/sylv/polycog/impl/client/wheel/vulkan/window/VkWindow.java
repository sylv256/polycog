package gay.sylv.polycog.impl.client.wheel.vulkan.window;

import static gay.sylv.polycog.impl.client.core.GameClient.callSDL;
import static gay.sylv.polycog.impl.client.core.GameClient.checkCallSDL;
import static org.lwjgl.sdl.SDLVideo.SDL_CreateWindow;
import static org.lwjgl.sdl.SDLVideo.SDL_DestroyWindow;
import static org.lwjgl.sdl.SDLVideo.SDL_SetWindowResizable;
import static org.lwjgl.sdl.SDLVideo.SDL_WINDOW_RESIZABLE;
import static org.lwjgl.sdl.SDLVideo.SDL_WINDOW_VULKAN;

import gay.sylv.polycog.api.client.wheel.window.Surface;
import gay.sylv.polycog.api.client.wheel.window.Window;
import gay.sylv.polycog.impl.client.wheel.NativeResource;
import gay.sylv.polycog.impl.client.wheel.vulkan.device.VkGpuDevice;
import gay.sylv.polycog.impl.client.wheel.vulkan.device.VkPhysicalGpuDevice;

public final class VkWindow extends NativeResource<Long> implements Window {
	private final VkGpuDevice device;
	private final VkSurface surface;
	private final int width;
	private final int height;
	private boolean resizable = true;

	public VkWindow(
			VkGpuDevice device,
			int width,
			int height,
			String name
	) {
		this.vkHandle = callSDL(() -> SDL_CreateWindow(
				name,
				width,
				height,
				SDL_WINDOW_VULKAN | SDL_WINDOW_RESIZABLE
		), "Failed to initialize window");
		this.width = width;
		this.height = height;

		this.device = device;
		this.surface = this.addChild(new VkSurface(this));
		device.addChild(this);
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void setResizable(boolean resizable) {
		checkCallSDL(() -> SDL_SetWindowResizable(this.getVkHandle(), resizable));
		this.resizable = resizable;
	}

	@Override
	public boolean isResizable() {
		return resizable;
	}

	@Override
	public Surface getSurface() {
		return surface;
	}

	public VkGpuDevice getDevice() {
		return device;
	}

	public VkPhysicalGpuDevice getPhysicalDevice() {
		return device.getPhysicalDevice();
	}

	@Override
	protected void onFree() {
		callSDL(() -> SDL_DestroyWindow(this.getVkHandle()));
	}
}
