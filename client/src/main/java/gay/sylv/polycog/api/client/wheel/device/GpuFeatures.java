/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.api.client.wheel.device;

import gay.sylv.polycog.impl.client.wheel.RenderObject;
import gay.sylv.polycog.impl.client.wheel.vulkan.device.VkGpuFeatures;

/// A set of features a particular [GpuDevice] enables or a [PhysicalDevice]
/// supports.
public sealed interface GpuFeatures extends AutoCloseable, RenderObject<GpuFeatures> permits VkGpuFeatures {
	/// A feature set containing core features required for Wheel to function.
	GpuFeatures CORE = VkGpuFeatures.DEFAULT;

	/// @return whether this feature set has all the core features required for
	/// Wheel to function.
	boolean hasCoreFeatures();

	/// @return whether this feature set has all the requested features.
	boolean hasFeatures(GpuFeatures requestedFeatures);
}
