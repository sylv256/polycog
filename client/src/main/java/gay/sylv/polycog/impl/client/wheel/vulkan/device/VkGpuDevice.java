package gay.sylv.polycog.impl.client.wheel.vulkan.device;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueue;

import gay.sylv.polycog.api.client.wheel.device.GpuDevice;
import gay.sylv.polycog.api.client.wheel.device.GpuQueue;
import gay.sylv.polycog.api.client.wheel.device.GpuQueueType;
import gay.sylv.polycog.impl.client.wheel.GameRenderer;
import gay.sylv.polycog.impl.client.wheel.NativeResource;
import gay.sylv.polycog.impl.client.wheel.vulkan.core.VkResult;

public final class VkGpuDevice extends NativeResource implements GpuDevice {
	private final VkDevice vkHandle;
	private final Collection<GpuQueue> gpuQueues;
	private @Nullable GpuQueue graphicsQueue;

	public VkGpuDevice(
			MemoryStack stack,
			VkPhysicalDevice vkPhysicalDevice,
			VkDeviceCreateInfo createInfo,
			Collection<VkGpuQueueFamily> queueFamilies
	) {
		PointerBuffer deviceBuffer = this.mallocPointer();
		GameRenderer.assertSuccess(VkResult.fromRaw(VK13.vkCreateDevice(
				vkPhysicalDevice,
				createInfo,
				null,
				deviceBuffer
		)), "Failed to create VkDevice");
		this.vkHandle = new VkDevice(deviceBuffer.get(0), vkPhysicalDevice, createInfo);

		// Assume most Queue Families have around 2 queues.
		List<GpuQueue> queues = new ArrayList<>(queueFamilies.size() * 2);

		for (VkGpuQueueFamily queueFamily : queueFamilies) {
			for (int j = 0; j < queueFamily.createInfo().queueCount(); j++) {
				PointerBuffer queuesBuffer = stack.mallocPointer(1);
				VK13.vkGetDeviceQueue(this.vkHandle, queueFamily.index(), j, queuesBuffer);
				VkGpuQueue queue = new VkGpuQueue(
						new VkQueue(queuesBuffer.get(), this.vkHandle),
						queueFamily
				);
				queues.add(queue);

				if (this.graphicsQueue == null && queueFamily.type().equals(GpuQueueType.GRAPHICS)) {
					this.graphicsQueue = queue;
				}
			}
		}

		this.gpuQueues = List.copyOf(queues);
	}

	@Override
	public Collection<GpuQueue> getQueues() {
		return this.gpuQueues;
	}

	@Override
	public GpuQueue getGraphicsQueue() {
		return Objects.requireNonNull(this.graphicsQueue, "This GpuDevice does not have a graphics queue");
	}

	@Override
	public void onFree() {
		VK10.vkDestroyDevice(this.vkHandle, null);
	}
}
