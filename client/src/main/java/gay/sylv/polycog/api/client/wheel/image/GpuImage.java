package gay.sylv.polycog.api.client.wheel.image;

import gay.sylv.polycog.api.client.wheel.memory.GpuBuffer;

/// An image on the GPU backed by a [GpuBuffer].
public interface GpuImage {
	GpuBuffer getBuffer();
}
