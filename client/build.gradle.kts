plugins {
	id("conventions.polycog")
}

base.archivesName = "polycog-client"

val bootstrap = "gay/sylv/polycog/impl/client/bootstrap/MainClient"
val commonJar = "${rootProject.name}-common-${version}-slim.jar"

fun filesTxt(): File {
	return file(projectDir.absolutePath + "/build/resources/main/META-INF/jars/files.txt")
}

val lwjglNatives = Pair(
	System.getProperty("os.name")!!,
	System.getProperty("os.arch")!!
).let { (name, arch) ->
	when {
		arrayOf("Linux", "SunOS", "Unit").any { name.startsWith(it) } ->
			if (arch.startsWith("aarch64"))
				"natives-linux-arm64"
			else if (arch.startsWith("riscv"))
				"natives-linux-riscv64"
			else
				"natives-linux"
		arrayOf("Windows").any { name.startsWith(it) } ->
			"natives-windows"
		else ->
			throw Error("Unrecognized or unsupported platform. Please set \"lwjglNatives\" manually")
	}
}.let { name -> ":$name" }

sourceSets {
	main {
		resources {
			// This merges the resources from the common module with the client
			// module. The DuplicatesStrategy is EXCLUDE which allows us to
			// override common resource files with the same name in the client
			// module.
			srcDir("../common/src/main/resources")
		}
	}
}

dependencies {
	implementation(project(":common"))

	"include"(Libraries.LWJGL)
	"include"(Libraries.LWJGL + lwjglNatives)
	implementation(Libraries.LWJGL)
	implementation(Libraries.LWJGL + lwjglNatives)
	"include"(Libraries.LWJGL_GLFW)
	"include"(Libraries.LWJGL_GLFW + lwjglNatives)
	implementation(Libraries.LWJGL_GLFW)
	implementation(Libraries.LWJGL_GLFW + lwjglNatives)
	"include"(Libraries.LWJGL_STB)
	"include"(Libraries.LWJGL_STB + lwjglNatives)
	implementation(Libraries.LWJGL_STB)
	implementation(Libraries.LWJGL_STB + lwjglNatives)
	"include"(Libraries.LWJGL_VULKAN)
	implementation(Libraries.LWJGL_VULKAN)
	"include"(Libraries.LWJGL_VMA)
	"include"(Libraries.LWJGL_VMA + lwjglNatives)
	implementation(Libraries.LWJGL_VMA)
	implementation(Libraries.LWJGL_VMA + lwjglNatives)
}

tasks {
	withType<Jar> {
		manifest {
			attributes(
				"Main-Class" to bootstrap
			)
		}
	}

	withType<ProcessResources> {
		dependsOn(":common:slimJar")
		// This DuplicatesStrategy ensures that any overlapping resources in
		// common are overwritten with the client's resources instead.
		duplicatesStrategy = DuplicatesStrategy.EXCLUDE

		from("../common/build/libs/${commonJar}") {
			rename { "META-INF/jars/${it}" }
		}
		afterEvaluate {
			if (!filesTxt().readText().contains(commonJar)) {
				filesTxt().appendText("$commonJar\n")
			}
		}
	}

	application {
		mainClass = bootstrap
		applicationDefaultJvmArgs += "--enable-native-access=ALL-UNNAMED"
		applicationDefaultJvmArgs += "-Dorg.lwjgl.util.DebugLoader=true"
		applicationDefaultJvmArgs += "-Dorg.lwjgl.util.Debug=true"
	}

	register("runClient") {
		group = "polycog"
		dependsOn("run")
	}
}
