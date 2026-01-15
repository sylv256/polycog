import org.gradle.kotlin.dsl.dependencies

plugins {
	id("conventions.all")
	idea
	application
	`maven-publish`
}

fun jarsDir(): File {
	return file(projectDir.absolutePath + "/build/resources/main/META-INF/jars")
}

fun filesTxt(): File {
	return file(projectDir.absolutePath + "/build/resources/main/META-INF/jars/files.txt")
}

dependencies {
	implementation(project(":sprocket"))

	"include"("org.slf4j:slf4j-api:2.0.17")
	"include"("org.apache.logging.log4j:log4j-slf4j-impl:2.25.3")
	"include"("org.apache.logging.log4j:log4j-core:2.25.3")
	implementation("org.slf4j:slf4j-api:2.0.17")
	implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.25.3")
	implementation("org.apache.logging.log4j:log4j-core:2.25.3")

	testImplementation(platform("org.junit:junit-bom:5.10.0"))
	testImplementation("org.junit.jupiter:junit-jupiter")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

sourceSets {
	main {
		resources {
			srcDir("../sprocket/build/classes/java/main")
		}
	}
}

tasks {
	withType<Jar> {
		manifest {
			attributes(
				"Enable-Native-Access" to "ALL-UNNAMED"
			)
		}
	}

	withType<Test> {
		useJUnitPlatform()
	}

	withType<JavaCompile> {
		if (project.name === "client") {
			options.compilerArgs.addAll(listOf(
				"--add-reads",
				"gay.sylv.polycog.client=ALL-UNNAMED"
			))
		} else if (project.name === "common") {
			options.compilerArgs.addAll(listOf(
				"--add-reads",
				"gay.sylv.polycog=ALL-UNNAMED"
			))
		}
	}

	withType<ProcessResources> {
		dependsOn(":sprocket:compileJava")
		afterEvaluate {
			configurations["include"].isCanBeResolved = true

			jarsDir().mkdirs()
			val files = filesTxt()
			files.delete()
			val depList = mutableListOf<String>()
			configurations["include"].dependencies.forEach {
				depList += "${it.name}-${it.version}.jar"
			}
			configurations["include"].files.filter {
				!it.name.matches(Regex("sprocket-.*\\.jar"))
			}.sortedWith { file0, file1 ->
				val index0 = depList.indexOf(file0.name)
				val index1 = depList.indexOf(file1.name)

				if (index0 < 0) {
					return@sortedWith index1
				} else if (index1 < 0) {
					return@sortedWith index0
				}

				return@sortedWith index0 - index1
			}.forEach { file ->
				files.appendText("${file.name}\n")
				from(file.absolutePath) {
					rename { "META-INF/jars/$it" }
				}
			}
		}
	}

	application {
		this@application.applicationDefaultJvmArgs += "-Dpolycog.debug.ide=1"
	}

	getByName("run") {
		group = "other"
	}
}
