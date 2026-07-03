package gay.sylv.polycog.impl.client.wheel.vulkan;

import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK10;

import gay.sylv.polycog.api.client.wheel.image.ImageFormat;
import gay.sylv.polycog.api.client.wheel.surface.PresentMode;

public final class VkConstants {
	private VkConstants() {
	}

	public static int format(ImageFormat imageFormat) {
		return switch (imageFormat) {
			case BGRA32_SRGB -> VK10.VK_FORMAT_B8G8R8A8_SRGB;
		};
	}

	public static int colorSpace(ImageFormat imageFormat) {
		return switch (imageFormat) {
			case BGRA32_SRGB -> KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;
		};
	}

	public static int presentMode(PresentMode presentMode) {
		return switch (presentMode) {
			case IMMEDIATE -> KHRSurface.VK_PRESENT_MODE_IMMEDIATE_KHR;
			case MAILBOX -> KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR;
			case FIFO -> KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
			case FIFO_RELAXED -> KHRSurface.VK_PRESENT_MODE_FIFO_RELAXED_KHR;
		};
	}
}
