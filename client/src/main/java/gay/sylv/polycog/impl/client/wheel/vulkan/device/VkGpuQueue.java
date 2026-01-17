package gay.sylv.polycog.impl.client.wheel.vulkan.device;

import org.lwjgl.vulkan.VkQueue;

import gay.sylv.polycog.api.client.wheel.device.GpuQueue;
import gay.sylv.polycog.api.client.wheel.device.GpuQueueType;

public final class VkGpuQueue implements GpuQueue {
	private final VkQueue vkHandle;
	private final GpuQueueType type;
	private final VkGpuQueueFamily family;

	public VkGpuQueue(
			VkQueue vkHandle,
			VkGpuQueueFamily family
	) {
		this.vkHandle = vkHandle;
		this.type = family.type();
		this.family = family;
	}

	@Override
	public GpuQueueType type() {
		return this.type;
	}

	public VkQueue getVkHandle() {
		return vkHandle;
	}

	public VkGpuQueueFamily getFamily() {
		return family;
	}
}
