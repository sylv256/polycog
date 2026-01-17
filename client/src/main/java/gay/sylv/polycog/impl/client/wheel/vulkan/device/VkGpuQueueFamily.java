package gay.sylv.polycog.impl.client.wheel.vulkan.device;

import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import gay.sylv.polycog.api.client.wheel.device.GpuQueueType;

public record VkGpuQueueFamily(int index, GpuQueueType type, VkQueueFamilyProperties properties, VkDeviceQueueCreateInfo createInfo) {
}
