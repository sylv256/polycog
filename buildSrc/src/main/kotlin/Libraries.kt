import Versions.SLF4J
import Versions.LOG4J
import Versions.JSPECIFY as JSPECIFY_VER
import Versions.LWJGL as LWJGL_VER
import Versions.JETBRAINS_ANNOTATIONS as JETBRAINS_ANNOTATIONS_VER
import Versions.JOML as JOML_VER

object Libraries {
	// Game
	const val GAME_ID = "polycog"

	// Common
	const val JETBRAINS_ANNOTATIONS = "org.jetbrains:annotations:$JETBRAINS_ANNOTATIONS_VER"
	const val JACKSON_CORE = "com.fasterxml.jackson.core:jackson-core:2.21.1"
	const val JACKSON_DATAFORMAT_XML = "com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.21.1"
	const val JACKSON_DATAFORMAT_YAML = "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.21.1"
	const val DISRUPTOR_FRAMEWORK = "com.lmax:disruptor:3.4.4"
	const val OSGI_FRAMEWORK = "org.osgi:org.osgi.framework:1.10.0"
	const val OSGI_RESOURCE = "org.osgi:org.osgi.resource:1.0.1"
	const val OSGI_DTO = "org.osgi:org.osgi.dto:1.1.1"
	const val JAVAX_MAIL = "javax.mail:javax.mail-api:1.6.2"
	const val JAVAX_ACTIVATION = "javax.activation:activation:1.1.1"
	const val JCTOOLS_CORE = "org.jctools:jctools-core:4.0.6"
	const val SLF4J_API = "org.slf4j:slf4j-api:$SLF4J"
	const val LOG4J_SLF4J_IMPL =
		"org.apache.logging.log4j:log4j-slf4j2-impl:$LOG4J"
	const val LOG4J_SLF4J_12 = "org.apache.logging.log4j:log4j-1.2-api:$LOG4J"
	const val LOG4J_CORE = "org.apache.logging.log4j:log4j-core:$LOG4J"
	const val LOG4J_API = "org.apache.logging.log4j:log4j-api:$LOG4J"
	const val JSPECIFY = "org.jspecify:jspecify:$JSPECIFY_VER"
	const val JOML = "org.joml:joml:$JOML_VER"

	// Client
	const val LWJGL = "org.lwjgl:lwjgl:$LWJGL_VER"
	const val LWJGL_GLFW = "org.lwjgl:lwjgl-glfw:$LWJGL_VER"
	const val LWJGL_JEMALLOC = "org.lwjgl:lwjgl-jemalloc:$LWJGL_VER"
	const val LWJGL_STB = "org.lwjgl:lwjgl-stb:$LWJGL_VER"
	const val LWJGL_VULKAN = "org.lwjgl:lwjgl-vulkan:$LWJGL_VER"
	const val LWJGL_VMA = "org.lwjgl:lwjgl-vma:$LWJGL_VER"
}
