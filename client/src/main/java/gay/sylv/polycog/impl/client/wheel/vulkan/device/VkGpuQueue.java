/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.impl.client.wheel.vulkan.device;

import org.lwjgl.vulkan.VkQueue;

import gay.sylv.polycog.api.client.wheel.device.GpuQueue;
import gay.sylv.polycog.api.client.wheel.device.GpuQueueType;
import gay.sylv.polycog.impl.client.wheel.NativeResource;

public final class VkGpuQueue extends NativeResource<VkQueue> implements GpuQueue {
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

	public VkGpuQueueFamily getFamily() {
		return family;
	}
}
