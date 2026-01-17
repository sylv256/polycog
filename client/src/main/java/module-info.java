import org.jspecify.annotations.NullMarked;

@NullMarked
module gay.sylv.polycog.client {
	requires gay.sylv.polycog;
	requires org.slf4j;
	requires org.jspecify;
	requires org.lwjgl.glfw;
	requires org.lwjgl.vulkan;

	exports gay.sylv.polycog.api.client.wheel.device;
	exports gay.sylv.polycog.api.client.wheel.memory;
}
