package gay.sylv.polycog.api.client.wheel.device;

import gay.sylv.polycog.impl.client.wheel.vulkan.device.VkGpuFeatures;

/// A set of features a particular [GpuDevice] enables.
public sealed interface GpuFeatures permits VkGpuFeatures {
}
