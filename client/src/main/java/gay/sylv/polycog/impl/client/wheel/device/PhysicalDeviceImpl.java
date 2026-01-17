package gay.sylv.polycog.impl.client.wheel.device;

import static org.lwjgl.system.MemoryStack.stackPush;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceLimits;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;

import gay.sylv.polycog.api.client.wheel.device.PhysicalDevice;
import gay.sylv.polycog.impl.share.LazyConstant;

public final class PhysicalDeviceImpl implements PhysicalDevice {
	private final VkPhysicalDevice vkHandle;
	private final LazyConstant<VkPhysicalDeviceProperties> vkProperties = LazyConstant.of();
	private final LazyConstant<VkPhysicalDeviceLimits> vkLimits = LazyConstant.of();

	public PhysicalDeviceImpl(long handle, VkInstance vkInstance) {
		this.vkHandle = new VkPhysicalDevice(handle, vkInstance);
	}

	private VkPhysicalDeviceProperties getVkProperties() {
		return this.vkProperties.getOrSet(() -> {
			try (MemoryStack stack = stackPush()) {
				VkPhysicalDeviceProperties properties =
						VkPhysicalDeviceProperties.calloc(stack);
				VK13.vkGetPhysicalDeviceProperties(this.vkHandle, properties);
				return properties;
			}
		});
	}

	private VkPhysicalDeviceLimits getVkLimits() {
		return this.vkLimits.getOrSet(() -> {
			return this.getVkProperties().limits();
		});
	}

	@Override
	public String name() {
		return this.getVkProperties().deviceNameString();
	}
}
