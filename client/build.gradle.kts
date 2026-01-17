plugins {
	id("conventions.polycog")
}

base.archivesName = "polycog-client"

val bootstrap = "gay/sylv/polycog/impl/client/bootstrap/MainClient"
val commonJar = "${rootProject.name}-common-${version}-slim.jar"

fun filesTxt(): File {
	return file(projectDir.absolutePath + "/build/resources/main/META-INF/jars/files.txt")
}

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
	implementation(Libraries.LWJGL)
	"include"(Libraries.LWJGL_GLFW)
	implementation(Libraries.LWJGL_GLFW)
	"include"(Libraries.LWJGL_STB)
	implementation(Libraries.LWJGL_STB)
	"include"(Libraries.LWJGL_VULKAN)
	implementation(Libraries.LWJGL_VULKAN)
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
	}

	register("runClient") {
		group = "polycog"
		dependsOn("run")
	}
}
