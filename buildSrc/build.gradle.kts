plugins {
	kotlin("jvm") version "2.2.20"
	`kotlin-dsl`
	kotlin("plugin.serialization") version "2.2.20"
}

repositories {
	gradlePluginPortal()
}

dependencies {
	implementation("com.diffplug.spotless:spotless-plugin-gradle:7.2.1")
	implementation("io.github.joselion:strict-null-check:3.5.0")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
	implementation("org.jetbrains.kotlin:kotlin-serialization-compiler-plugin-embeddable:2.2.21")
}
