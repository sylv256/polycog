/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.api.client.wheel.device;

import gay.sylv.polycog.impl.client.wheel.vulkan.device.VkPhysicalGpuDevice;

/// A physical GPU device that contains
/// [logical GPU devices][GpuDevice].
public sealed interface PhysicalGpuDevice extends AutoCloseable permits VkPhysicalGpuDevice {
	String name();

	GpuDevice getLogicalGpuDevice();
}
