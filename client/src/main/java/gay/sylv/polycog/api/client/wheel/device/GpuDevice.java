/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.api.client.wheel.device;

import java.util.Collection;

import gay.sylv.polycog.impl.client.wheel.RenderObject;
import gay.sylv.polycog.impl.client.wheel.vulkan.device.VkGpuDevice;

/// A logical GPU device. Typically, this has a specific function in a
/// [PhysicalGpuDevice].
public sealed interface GpuDevice extends AutoCloseable, RenderObject<GpuDevice> permits VkGpuDevice {
	/// @return the currently selected [GpuDevice].
	static GpuDevice get() {
		return PhysicalGpuDevice.get().getLogicalDevice(GpuFeatures.CORE);
	}

	Collection<GpuQueue> getQueues();

	/// @return the first suitable [GpuQueue] of [GpuQueueType#GRAPHICS].
	/// @throws NullPointerException if this device does not support graphics.
	GpuQueue getGraphicsQueue();
}
