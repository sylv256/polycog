package gay.sylv.polycog.api.client.wheel.image;

import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK10;

public enum ImageFormat {
	BGRA32_SRGB(VK10.VK_FORMAT_B8G8R8A8_SRGB, KHRSurface.VK_COLORSPACE_SRGB_NONLINEAR_KHR);

	private final int vkFormat;
	private final int vkColorSpace;

	ImageFormat(int vkFormat, int vkColorSpace) {
		this.vkFormat = vkFormat;
		this.vkColorSpace = vkColorSpace;
	}

	@ApiStatus.Internal
	public int getVkFormat() {
		return vkFormat;
	}

	@ApiStatus.Internal
	public int getVkColorSpace() {
		return vkColorSpace;
	}
}
