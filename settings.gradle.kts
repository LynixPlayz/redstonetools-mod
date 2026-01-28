pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()

        maven {
			url = uri("https://maven.fabricmc.net/")
		}
        maven {
            name = "KikuGie Snapshots"
            url = uri("https://maven.kikugie.dev/snapshots")
        }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.8.3"
}

rootProject.name = "redstonetools-mod"

include("1.21.4")
include("1.21.5")
include("1.21.8")
include("1.21.10")
include("1.21.11")

stonecutter {
    centralScript = "build.gradle.kts"

    create(rootProject) {
        versions("1.21.4", "1.21.5", "1.21.8", "1.21.10", "1.21.11")
		vcsVersion = "1.21.11"
    }
}
