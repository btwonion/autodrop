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
    id("dev.kikugie.stonecutter") version "0.9"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"
    shared {
        version("26.1-fabric", "26.1")
        version("26.1-neoforge", "26.1")
        vcsVersion = "26.1-fabric"
    }
    create(rootProject)
}
