plugins {
	id("conventions.polycog")
}

base.archivesName = "polycog-client"

val bootstrap = "gay/sylv/polycog/impl/client/bootstrap/MainClient"
val commonJar = "${rootProject.name}-common-${version}-slim.jar"

fun filesTxt(): File {
	return file(projectDir.absolutePath + "/build/resources/main/META-INF/jars/files.txt")
}

val lwjglSupportedNatives = listOf("natives-linux-arm64", "natives-linux-riscv64", "natives-linux", "natives-windows")

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

fun DependencyHandlerScope.includeNatives(lib: String) {
	this.add("include", lib)

	lwjglSupportedNatives.forEach {
		if (!(lib.contains("vulkan") && !it.contains("macos"))) {
			this.add("include", "$lib:$it")
		}
	}
}

fun DependencyHandlerScope.implementationIncludeNatives(lib: String) {
	this.includeNatives(lib)

	this.add("implementation", lib)

	if (!(lib.contains("vulkan") && !lwjglNatives.contains("macos"))) {
		this.add("implementation", lib + lwjglNatives)
	}
}

dependencies {
	implementation(project(":common"))

	implementationIncludeNatives(Libraries.LWJGL)
	implementationIncludeNatives(Libraries.LWJGL_JEMALLOC)
	implementationIncludeNatives(Libraries.LWJGL_GLFW)
	implementationIncludeNatives(Libraries.LWJGL_STB)
	implementationIncludeNatives(Libraries.LWJGL_VULKAN)
	implementationIncludeNatives(Libraries.LWJGL_VMA)
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

	getByName("run") {
		group = "polycog"
	}
}
