/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.api.client.wheel.device;

import org.jspecify.annotations.Nullable;

import gay.sylv.polycog.impl.client.wheel.GameRenderer;
import gay.sylv.polycog.impl.client.wheel.vulkan.device.VkPhysicalGpuDevice;

/// A physical GPU device that contains
/// [logical GPU devices][GpuDevice].
public sealed interface PhysicalGpuDevice extends AutoCloseable permits VkPhysicalGpuDevice {
	static PhysicalGpuDevice get() {
		return GameRenderer.getInstance().getPhysicalGpuDevice();
	}

	String name();

	/// Get the logical device with the given [GpuFeatures].
	GpuDevice getLogicalDevice(@Nullable GpuFeatures features);
}
