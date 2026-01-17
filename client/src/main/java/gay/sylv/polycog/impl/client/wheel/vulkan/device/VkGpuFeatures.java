package gay.sylv.polycog.impl.client.wheel.vulkan.device;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;

import gay.sylv.polycog.api.client.wheel.device.GpuFeatures;

public record VkGpuFeatures(
		boolean samplerAnisotropy
) implements GpuFeatures {
	public static final VkGpuFeatures DEFAULT = new VkGpuFeatures(true);
	private static final Set<VkGpuFeatures> VALUES = new HashSet<>();
	private static final Map<VkGpuFeatures, VkPhysicalDeviceFeatures2> RAW = new HashMap<>();
}
