package gay.sylv.polycog.impl.client.wheel.vulkan.device;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueue;

import gay.sylv.polycog.api.client.wheel.device.GpuDevice;
import gay.sylv.polycog.api.client.wheel.device.GpuQueue;
import gay.sylv.polycog.api.client.wheel.device.GpuQueueType;
import gay.sylv.polycog.impl.client.core.GameClient;
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
		var queueCreateInfosBuffer = new VkDeviceQueueCreateInfo.Buffer(stack.calloc(VkDeviceQueueCreateInfo.SIZEOF * queueFamilies.size()));

		for (VkGpuQueueFamily queueFamily : queueFamilies) {
			VkDeviceQueueCreateInfo queueCreateInfo = VkDeviceQueueCreateInfo.calloc(stack);
			queueCreateInfo.queueFamilyIndex(queueFamily.index());
			queueCreateInfo.pQueuePriorities(stack.floats(1.0f));
			queueCreateInfosBuffer.put(queueCreateInfo);
		}

		createInfo.pQueueCreateInfos(queueCreateInfosBuffer);
		GameRenderer.assertSuccess(VkResult.fromRaw(VK13.vkCreateDevice(
				vkPhysicalDevice,
				createInfo,
				GameClient.getInstance().getRenderer().getAllocator(),
				deviceBuffer
		)), "Failed to create VkDevice");
		this.vkHandle = new VkDevice(deviceBuffer.get(0), vkPhysicalDevice, createInfo);

		PointerBuffer queuesBuffer = stack.callocPointer(queueFamilies.size());
		GpuQueue[] queues = new VkGpuQueue[queueFamilies.size()];
		int i = 0;

		for (VkGpuQueueFamily queueFamily : queueFamilies) {
			VK13.vkGetDeviceQueue(this.vkHandle, queueFamily.index(), i, queuesBuffer);
			queues[i] = new VkGpuQueue(
					new VkQueue(queuesBuffer.get(), this.vkHandle),
					queueFamily
			);

			if (this.graphicsQueue == null && queueFamily.type().equals(GpuQueueType.GRAPHICS)) {
				this.graphicsQueue = queues[i];
			}

			i++;
		}

		this.gpuQueues = List.of(queues);
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
		VK13.vkDestroyDevice(this.vkHandle, GameClient.getInstance().getRenderer().getAllocator());
	}
}
