package gay.sylv.polycog.impl.client.wheel.vulkan.window;

import static gay.sylv.polycog.impl.client.core.GameClient.handleErrorSDL;
import static org.lwjgl.sdl.SDLVulkan.SDL_Vulkan_CreateSurface;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import gay.sylv.polycog.api.client.wheel.image.ImageFormat;
import gay.sylv.polycog.api.client.wheel.window.Surface;
import gay.sylv.polycog.impl.client.wheel.GameRenderer;
import gay.sylv.polycog.impl.client.wheel.NativeResource;
import gay.sylv.polycog.impl.client.wheel.vulkan.core.DeviceUnsupportedException;

public final class VkSurface extends NativeResource<LongBuffer> implements Surface {
	private final int vkSurface = this.getMemberIndex();
	private final int vkSwapchain = this.getMemberIndex();
	private final VkSurfaceCapabilitiesKHR surfaceCapabilities =
			this.allocStruct(VkSurfaceCapabilitiesKHR::calloc);
	private final VkWindow window;

	public VkSurface(VkWindow window) {
		this.window = window;
		this.vkHandle = this.mallocLongs(2);

		if (!SDL_Vulkan_CreateSurface(
				window.getVkHandle(),
				GameRenderer.getInstance().getVkInstance(),
				null,
				this.getVkHandleRef(this.vkSurface)
		)) {
			throw handleErrorSDL(new DeviceUnsupportedException("Vulkan surfaces are unsupported"));
		}

		GameRenderer.assertSuccess(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(
				window.getPhysicalDevice().getVkHandle(),
				this.getVkHandle(this.vkSurface),
				this.surfaceCapabilities
		), "Failed to get surface capabilities");

		VkExtent2D swapchainExtent;

		if (this.surfaceCapabilities.currentExtent().width() == -1) {
			swapchainExtent = VkExtent2D.calloc()
					.width(window.getWidth())
					.height(window.getHeight());
		} else {
			swapchainExtent = VkExtent2D.malloc()
					.set(this.surfaceCapabilities.currentExtent());
		}

		try (VkSwapchainCreateInfoKHR swapchainCI = VkSwapchainCreateInfoKHR.calloc()
				.sType$Default()
				.surface(this.getVkHandle(this.vkSurface))
				.minImageCount(this.surfaceCapabilities.minImageCount())
				.imageFormat(this.getFormat().getVkFormat())
				.imageColorSpace(this.getFormat().getVkColorSpace())
				.imageExtent(swapchainExtent)
				.imageArrayLayers(1)
				.imageUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
				.preTransform(KHRSurface.VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR)
				.compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
				.presentMode(KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR)) {
			GameRenderer.assertSuccess(KHRSwapchain.vkCreateSwapchainKHR(
					window.getDevice().getVkHandle(),
					swapchainCI,
					null,
					this.getVkHandleRef(this.vkSwapchain)
			));
		}

		swapchainExtent.close();
	}

	@Override
	protected void onFree() {
		KHRSwapchain.vkDestroySwapchainKHR(
				this.window.getDevice().getVkHandle(),
				this.getVkHandle(this.vkSwapchain),
				null
		);
		KHRSurface.vkDestroySurfaceKHR(
				GameRenderer.getInstance().getVkInstance(),
				this.getVkHandle(this.vkSurface),
				null
		);
	}

	@Override
	public ImageFormat getFormat() {
		return ImageFormat.BGRA32_SRGB;
	}
}
