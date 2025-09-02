rootProject.name = "autodrop"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.10"
}


buildscript {
    repositories { mavenCentral() }
    dependencies {
        classpath("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    }
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"
    shared {
        version("1.20.6-neoforge", "1.20.6")
        version("1.20.6-fabric", "1.20.6")
        version("1.21-fabric", "1.21")
        version("1.21-neoforge", "1.21")
        version("1.21.3-fabric", "1.21.3")
        version("1.21.3-neoforge", "1.21.3")
        version("1.21.4-fabric", "1.21.4")
        version("1.21.4-neoforge", "1.21.4")
        version("1.21.5-fabric", "1.21.5")
        version("1.21.5-neoforge", "1.21.5")
        version("1.21.6-fabric", "1.21.6")
        version("1.21.6-neoforge", "1.21.6")
        vcsVersion = "1.21.6-fabric"
    }
    create(rootProject)
}
