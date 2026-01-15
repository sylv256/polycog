plugins {
	id("conventions.polycog")
}

base.archivesName = "polycog-client"

val bootstrap = "gay/sylv/polycog/impl/client/bootstrap/MainClient"
val commonJar = "${rootProject.name}-common-${version}-slim.jar"

fun filesTxt(): File {
	return file(projectDir.absolutePath + "/build/resources/main/META-INF/jars/files.txt")
}

dependencies {
	implementation(project(":common"))
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
