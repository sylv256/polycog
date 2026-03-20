import org.jspecify.annotations.NullMarked;

@NullMarked
module gay.sylv.polycog.client {
	requires gay.sylv.polycog;
	requires org.jetbrains.annotations;
	requires org.apache.logging.log4j.core;
	requires org.apache.logging.log4j;
	requires org.slf4j;
	requires org.jspecify;
	requires org.lwjgl.vulkan;
	requires org.lwjgl.vma;
	requires org.lwjgl.sdl;

	exports gay.sylv.polycog.api.client.wheel.device;
	exports gay.sylv.polycog.api.client.wheel.memory;
}
