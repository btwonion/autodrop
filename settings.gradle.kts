import dev.kikugie.stonecutter.StonecutterSettings

rootProject.name = "autodrop"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.kikugie.dev/releases")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.4.+"
}


buildscript {
    repositories { mavenCentral() }
    dependencies {
        classpath("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    }
}

extensions.configure<StonecutterSettings> {
    kotlinController = true
    centralScript = "build.gradle.kts"
    shared {
        vers("1.20.4-neoforge", "1.20.4")
        vers("1.20.4-fabric", "1.20.4")
        vers("1.20.6-neoforge", "1.20.6")
        vers("1.20.6-fabric", "1.20.6")
        vers("1.21-neoforge", "1.21")
        vers("1.21-fabric", "1.21")
        vcsVersion = "1.21-fabric"
    }
    create(rootProject)
}
