/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.impl.client.wheel.vulkan.device;

import static org.lwjgl.system.MemoryStack.stackPush;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan11Features;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan12Features;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan13Features;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import gay.sylv.polycog.api.client.wheel.device.GpuDevice;
import gay.sylv.polycog.api.client.wheel.device.GpuFeatures;
import gay.sylv.polycog.api.client.wheel.device.GpuQueueType;
import gay.sylv.polycog.api.client.wheel.device.PhysicalGpuDevice;
import gay.sylv.polycog.impl.client.wheel.GameRenderer;
import gay.sylv.polycog.impl.client.wheel.NativeResource;
import gay.sylv.polycog.impl.client.wheel.vulkan.core.DeviceUnsupportedException;
import gay.sylv.polycog.impl.share.LazyConstant;
import gay.sylv.polycog.impl.share.LazyConstantList;
import gay.sylv.polycog.impl.share.LazyConstantMap;

public final class VkPhysicalGpuDevice extends NativeResource<VkPhysicalDevice> implements PhysicalGpuDevice {
	private final LazyConstant<VkPhysicalDeviceProperties> vkProperties = LazyConstant.of();
	private final LazyConstantList<VkQueueFamilyProperties> vkQueueFamilyProperties = LazyConstant.ofList();
	private final LazyConstantMap<Integer, VkGpuQueueFamily> gpuQueueFamilies = LazyConstant.ofMap();
	private final LazyConstantList<VkGpuDevice> gpuDevices = LazyConstant.ofList();
	private final LazyConstant<GpuFeatures> gpuFeatures = LazyConstant.of();
	private final Collection<String> extensions;

	public VkPhysicalGpuDevice(long handle, VkInstance vkInstance, Collection<String> extensions) {
		this.extensions = extensions;
		this.vkHandle = new VkPhysicalDevice(handle, vkInstance);
	}

	public VkPhysicalDeviceProperties getVkProperties() {
		return this.vkProperties.getOrSet(() -> {
			VkPhysicalDeviceProperties properties = this.allocStruct(VkPhysicalDeviceProperties::malloc);
			VK13.vkGetPhysicalDeviceProperties(this.getVkHandle(), properties);
			return properties;
		});
	}

	private List<VkQueueFamilyProperties> vkGetPhysicalDeviceQueueFamilyProperties() {
		return this.vkQueueFamilyProperties.getOrSet(() -> {
			try (MemoryStack stack = stackPush()) {
				IntBuffer count = stack.mallocInt(1);
				VK13.vkGetPhysicalDeviceQueueFamilyProperties(this.getVkHandle(), count, null);
				VkQueueFamilyProperties.Buffer buffer = this.allocStructs(count, VkQueueFamilyProperties::malloc);
				VK13.vkGetPhysicalDeviceQueueFamilyProperties(this.getVkHandle(), count, buffer);
				return GameRenderer.bufferToList(count, buffer);
			}
		});
	}

	@Override
	public String name() {
		return this.getVkProperties().deviceNameString();
	}

	@Override
	public GpuFeatures getSupportedFeatures() {
		return this.gpuFeatures.getOrSet(() -> {
			VkPhysicalDeviceVulkan11Features vk13Features = VkPhysicalDeviceVulkan11Features.calloc()
					.sType$Default();
			VkPhysicalDeviceVulkan12Features vk12Features = VkPhysicalDeviceVulkan12Features.calloc()
					.sType$Default();
			VkPhysicalDeviceVulkan13Features vk11Features = VkPhysicalDeviceVulkan13Features.calloc()
					.sType$Default();
			VkPhysicalDeviceFeatures2 features2 = VkPhysicalDeviceFeatures2.calloc()
					.sType$Default()
					.pNext(vk11Features)
					.pNext(vk12Features)
					.pNext(vk13Features);
			VK11.vkGetPhysicalDeviceFeatures2(this.getVkHandle(), features2);
			return new VkGpuFeatures(features2);
		});
	}

	public Map<Integer, VkGpuQueueFamily> getQueueFamilies() {
		return this.gpuQueueFamilies.getOrSet(() -> {
			// Find suitable Queues
			List<VkQueueFamilyProperties> propertiesCollection =
					this.vkGetPhysicalDeviceQueueFamilyProperties();
			Map<Integer, VkGpuQueueFamily> queueFamilies = new HashMap<>();
			int familyIndex = 0;
			FloatBuffer priorities = this.allocFloats(1.0f);

			for (VkQueueFamilyProperties properties : propertiesCollection) {
				boolean hasGraphics = (properties.queueFlags() & VK13.VK_QUEUE_GRAPHICS_BIT) != 0;
				boolean hasCompute = (properties.queueFlags() & VK13.VK_QUEUE_COMPUTE_BIT) != 0;
				boolean hasNone = !(hasGraphics || hasCompute);

				if (hasNone) {
					continue;
				}

				if (hasGraphics) {
					queueFamilies.put(
							familyIndex,
							new VkGpuQueueFamily(
									familyIndex,
									GpuQueueType.GRAPHICS,
									properties,
									this.allocStruct(VkDeviceQueueCreateInfo::calloc)
											.sType$Default()
											.queueFamilyIndex(familyIndex)
											.pQueuePriorities(priorities)
							)
					);
					familyIndex++;
				}

				if (hasCompute) {
					queueFamilies.put(
							familyIndex,
							new VkGpuQueueFamily(
									familyIndex,
									GpuQueueType.COMPUTE,
									properties,
									this.allocStruct(VkDeviceQueueCreateInfo::calloc)
											.sType$Default()
											.queueFamilyIndex(familyIndex)
											.pQueuePriorities(priorities)
							)
					);
					familyIndex++;
				}
			}

			return queueFamilies;
		});
	}

	@Override
	public GpuDevice getLogicalDevice(GpuFeatures features) {
		return this.gpuDevices.getOrSet(() -> {
			try (MemoryStack stack = stackPush()) {
				if (!this.getSupportedFeatures().hasFeatures(features)) {
					throw new DeviceUnsupportedException("The requested Vulkan features are missing: " + features);
				}

				VkPhysicalDeviceFeatures2 vk2Features = features
						.<VkGpuFeatures>wheel$internal()
						.getFeatures2();
				VkDeviceCreateInfo createInfo = VkDeviceCreateInfo.calloc(stack)
						.sType$Default()
						.pNext(vk2Features)
						.ppEnabledExtensionNames(GameRenderer.collectionToBuffer(
								stack,
								this.extensions,
								MemoryStack::ASCII
						))
						.pQueueCreateInfos(GameRenderer.collectionToStructBuffer(
								stack,
								this.getQueueFamilies().values()
										.stream()
										.map(VkGpuQueueFamily::createInfo)
										.toList(),
								VkDeviceQueueCreateInfo::malloc
						));
				return List.of(this.addChildren(new VkGpuDevice(
						stack,
						this.getVkHandle(),
						createInfo,
						this.getQueueFamilies().values()
				)));
			}
		}).getFirst();
	}
}
