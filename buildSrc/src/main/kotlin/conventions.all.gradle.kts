import com.diffplug.spotless.LineEnding

plugins {
	id("java")
	checkstyle
	id("com.diffplug.spotless")
//	id("io.github.joselion.strict-null-check")
}

group = "gay.sylv"
version = Versions.GAME

repositories {
	mavenCentral()
}

configurations {
	register("include") {
		isCanBeResolved = false
	}
}

tasks {
	withType<JavaCompile> {
//		dependsOn("generatePackageInfo")
		dependsOn("spotlessApply")
	}

//	withType<GeneratePackageInfoTask> {
////		dependsOn("spotlessApply")
//	}
}

spotless {
	lineEndings = LineEnding.UNIX

	java {
		licenseHeaderFile("../HEADER")
		removeUnusedImports()
		importOrder("java", "javax", "", "net.fabricmc", "gay.sylv")
		leadingSpacesToTabs()
		trimTrailingWhitespace()
	}
}

checkstyle {
	configFile = file("../checkstyle.xml")
	toolVersion = "10.20.2"
}

// Bugged
//strictNullCheck {
//	packageInfo {
//		imports.set(setOf("org.jspecify.annotations.NullMarked"))
//		annotations.set(setOf("@NullMarked"))
//		javadoc.set("Auto-generated package-info.")
//	}
//}
