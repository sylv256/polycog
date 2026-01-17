plugins {
	id("conventions.polycog")
}

base.archivesName = "polycog-common"

val bootstrap = "gay/sylv/polycog/impl/bootstrap/MainServer"

dependencies {
}

tasks {
	withType<Jar> {
		manifest {
			attributes(
				"Main-Class" to bootstrap
			)
		}
	}

	register<Jar>("slimJar") {
		group = "build"
		from(sourceSets["main"].output)
		exclude {
			it.path.contains("META-INF/jars") ||
				it.path.contains("META-INF\\jars")
		}
		archiveClassifier = "slim"
	}

	application {
		mainClass = bootstrap
	}

	register("runServer") {
		group = "polycog"
		dependsOn("run")
	}
}
