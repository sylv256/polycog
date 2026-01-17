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

val commonJar = "${rootProject.name}-common-${version}-slim.jar"

dependencies {
	implementation(project(":sprocket"))

	"include"(Libraries.JETBRAINS_ANNOTATIONS)
	implementation(Libraries.JETBRAINS_ANNOTATIONS)
	"include"(Libraries.SLF4J_API)
	implementation(Libraries.SLF4J_API)
	"include"(Libraries.LOG4J_API)
	implementation(Libraries.LOG4J_API)
	"include"(Libraries.LOG4J_CORE)
	implementation(Libraries.LOG4J_CORE)
	"include"(Libraries.LOG4J_SLF4J_IMPL)
	implementation(Libraries.LOG4J_SLF4J_IMPL)

	"include"(Libraries.JSPECIFY)
	implementation(Libraries.JSPECIFY)

	"include"(Libraries.JOML)
	implementation(Libraries.JOML)

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
		val expansions = mapOf(
			"id" to Libraries.GAME_ID,
			"version" to Versions.GAME,
			"common_jar" to commonJar
		)

		filesMatching("fabric.mod.json") {
			expand(expansions)
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
