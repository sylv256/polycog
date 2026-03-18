/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.api.client.wheel.device;

import gay.sylv.polycog.impl.client.wheel.RenderObject;
import gay.sylv.polycog.impl.client.wheel.vulkan.device.VkGpuQueue;

/// A purposed system that manages the execution of encoded commands on the GPU.
public sealed interface GpuQueue extends RenderObject<GpuQueue> permits VkGpuQueue {
	GpuQueueType type();
}
