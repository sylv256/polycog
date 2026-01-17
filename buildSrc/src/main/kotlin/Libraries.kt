import Versions.SLF4J
import Versions.LOG4J
import Versions.JSPECIFY as JSPECIFY_VER
import Versions.LWJGL as LWJGL_VER

object Libraries {
	// Game
	const val GAME_ID = "polycog"

	// Common
	const val SLF4J_API = "org.slf4j:slf4j-api:$SLF4J"
	const val LOG4J_SLF4J_IMPL =
		"org.apache.logging.log4j:log4j-slf4j-impl:$LOG4J"
	const val LOG4J_CORE = "org.apache.logging.log4j:log4j-core:$LOG4J"
	const val LOG4J_API = "org.apache.logging.log4j:log4j-api:$LOG4J"
	const val JSPECIFY = "org.jspecify:jspecify:$JSPECIFY_VER"

	// Client
	const val LWJGL = "org.lwjgl:lwjgl:$LWJGL_VER"
	const val LWJGL_GLFW = "org.lwjgl:lwjgl-glfw:$LWJGL_VER"
	const val LWJGL_STB = "org.lwjgl:lwjgl-stb:$LWJGL_VER"
	const val LWJGL_VULKAN = "org.lwjgl:lwjgl-vulkan:$LWJGL_VER"
}
