package gay.sylv.polycog.api.client.wheel.surface;

import gay.sylv.polycog.api.client.wheel.image.ImageFormat;
import gay.sylv.polycog.impl.client.wheel.RenderObject;
import gay.sylv.polycog.impl.client.wheel.vulkan.window.VkSurface;

public sealed interface Surface extends AutoCloseable, RenderObject<Surface> permits VkSurface {
	ImageFormat getFormat();

	PresentMode getPresentMode();
}
