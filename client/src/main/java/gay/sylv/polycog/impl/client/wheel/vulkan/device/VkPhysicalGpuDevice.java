package gay.sylv.polycog.impl.client.wheel.vulkan.device;

import static org.lwjgl.system.MemoryStack.stackPush;

import java.nio.IntBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceLimits;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan12Features;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan13Features;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import gay.sylv.polycog.api.client.wheel.device.GpuDevice;
import gay.sylv.polycog.api.client.wheel.device.GpuQueueType;
import gay.sylv.polycog.api.client.wheel.device.PhysicalGpuDevice;
import gay.sylv.polycog.impl.client.wheel.GameRenderer;
import gay.sylv.polycog.impl.client.wheel.NativeResource;
import gay.sylv.polycog.impl.share.LazyConstant;
import gay.sylv.polycog.impl.share.LazyConstantList;
import gay.sylv.polycog.impl.share.LazyConstantMap;

public final class VkPhysicalGpuDevice extends NativeResource implements PhysicalGpuDevice {
	private final VkPhysicalDevice vkHandle;
	private final LazyConstant<VkPhysicalDeviceProperties> vkProperties = LazyConstant.of();
	private final LazyConstant<VkPhysicalDeviceLimits> vkLimits = LazyConstant.of();
	private final LazyConstantList<VkQueueFamilyProperties> vkQueueFamilyProperties = LazyConstant.ofList();
	private final LazyConstantMap<Integer, VkGpuQueueFamily> gpuQueueFamilies = LazyConstant.ofMap();
	private final LazyConstantList<VkGpuDevice> gpuDevices = LazyConstant.ofList();
	private final Collection<String> extensions;

	public VkPhysicalGpuDevice(long handle, VkInstance vkInstance, Collection<String> extensions) {
		this.extensions = extensions;
		this.vkHandle = new VkPhysicalDevice(handle, vkInstance);
	}

	private VkPhysicalDeviceProperties getVkProperties() {
		return this.vkProperties.getOrSet(() -> {
			VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.malloc();
			VK13.vkGetPhysicalDeviceProperties(this.vkHandle, properties);
			return properties;
		});
	}

	private VkPhysicalDeviceLimits getVkLimits() {
		return this.vkLimits.getOrSet(() -> this.getVkProperties().limits());
	}

	private List<VkQueueFamilyProperties> vkGetPhysicalDeviceQueueFamilyProperties() {
		return this.vkQueueFamilyProperties.getOrSet(() -> {
			try (MemoryStack stack = stackPush()) {
				IntBuffer count = stack.mallocInt(1);
				VK13.vkGetPhysicalDeviceQueueFamilyProperties(this.vkHandle, count, null);
				VkQueueFamilyProperties.Buffer buffer = this.allocStructs(count, VkQueueFamilyProperties::malloc);
				VK13.vkGetPhysicalDeviceQueueFamilyProperties(this.vkHandle, count, buffer);
				return GameRenderer.bufferToList(count, buffer);
			}
		});
	}

	@Override
	public String name() {
		return this.getVkProperties().deviceNameString();
	}

	public Map<Integer, VkGpuQueueFamily> getQueueFamilies() {
		return this.gpuQueueFamilies.getOrSet(() -> {
			// Find suitable Queues
			List<VkQueueFamilyProperties> propertiesCollection =
					this.vkGetPhysicalDeviceQueueFamilyProperties();
			Map<Integer, VkGpuQueueFamily> queueFamilies = new HashMap<>();

			for (int i = 0; i < propertiesCollection.size(); i++) {
				VkQueueFamilyProperties properties = propertiesCollection.get(i);

				boolean hasGraphics = (properties.queueFlags() & VK13.VK_QUEUE_GRAPHICS_BIT) != 0;
				boolean hasCompute = (properties.queueFlags() & VK13.VK_QUEUE_COMPUTE_BIT) != 0;
				boolean hasNone = !(hasGraphics || hasCompute);

				if (hasNone) {
					continue;
				}

				if (hasGraphics) {
					queueFamilies.put(i, new VkGpuQueueFamily(
							i,
							GpuQueueType.GRAPHICS,
							properties,
							this.allocStruct(VkDeviceQueueCreateInfo::calloc).sType$Default()
					));
				}

				if (hasCompute) {
					queueFamilies.put(i, new VkGpuQueueFamily(
							i,
							GpuQueueType.COMPUTE,
							properties,
							this.allocStruct(VkDeviceQueueCreateInfo::calloc).sType$Default()
					));
				}
			}

			return queueFamilies;
		});
	}

	@Override
	public GpuDevice getLogicalGpuDevice() {
		return this.gpuDevices.getOrSet(() -> {
			try (MemoryStack stack = stackPush()) {
				VkPhysicalDeviceVulkan12Features vk12Features = VkPhysicalDeviceVulkan12Features.calloc(stack)
						.sType$Default()
						.descriptorIndexing(true)
						.descriptorBindingVariableDescriptorCount(true)
						.runtimeDescriptorArray(true)
						.bufferDeviceAddress(true);
				VkPhysicalDeviceVulkan13Features vk13Features = VkPhysicalDeviceVulkan13Features.calloc(stack)
						.sType$Default()
						.synchronization2(true)
						.dynamicRendering(true)
						.pNext(vk12Features.address());
				VkPhysicalDeviceFeatures vk10Features = VkPhysicalDeviceFeatures.calloc(stack)
						.samplerAnisotropy(true);

				VkDeviceCreateInfo createInfo = VkDeviceCreateInfo.calloc(stack)
						.sType$Default()
						.pNext(vk13Features)
						.pEnabledFeatures(vk10Features)
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
				return List.of(new VkGpuDevice(stack, this.vkHandle, createInfo, this.getQueueFamilies().values()));
			}
		}).getFirst();
	}

	@Override
	protected void onFree() {
		this.vkQueueFamilyProperties.forEach(VkQueueFamilyProperties::close);
		this.gpuQueueFamilies.forEach((_, gpuQueueFamily) -> gpuQueueFamily.close());
		this.vkProperties.run(VkPhysicalDeviceProperties::close);
		this.gpuDevices.forEach(VkGpuDevice::close);
	}
}
