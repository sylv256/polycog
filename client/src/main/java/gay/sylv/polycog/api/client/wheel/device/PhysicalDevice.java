/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.api.client.wheel.device;

import gay.sylv.polycog.impl.client.wheel.GameRenderer;
import gay.sylv.polycog.impl.client.wheel.RenderObject;
import gay.sylv.polycog.impl.client.wheel.vulkan.device.VkPhysicalGpuDevice;

/// A physical GPU device that contains
/// [logical GPU devices][GpuDevice].
public sealed interface PhysicalDevice extends AutoCloseable, RenderObject<PhysicalDevice> permits VkPhysicalGpuDevice {
	static PhysicalDevice get() {
		return GameRenderer.getInstance().getPhysicalGpuDevice();
	}

	String name();

	/// @return the logical device with the given [GpuFeatures].
	GpuDevice getLogicalDevice(GpuFeatures features);

	/// @return the features this physical device supports.
	GpuFeatures getSupportedFeatures();
}
