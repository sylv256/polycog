import com.diffplug.spotless.LineEnding

plugins {
	id("java")
	checkstyle
	id("com.diffplug.spotless")
}

group = "gay.sylv"
version = "0.1.0"

repositories {
	mavenCentral()
}

configurations {
	register("include") {
		isCanBeResolved = false
	}
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
