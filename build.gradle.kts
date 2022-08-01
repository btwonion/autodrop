import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    id("fabric-loom") version "0.12-SNAPSHOT"
    id("io.github.juuxel.loom-quiltflower") version "1.7.3"
    id("org.quiltmc.quilt-mappings-on-loom") version "4.2.1"
}

group = "dev.nyon"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:1.19.1")
    mappings(loom.layered {
        addLayer(quiltMappings.mappings("org.quiltmc:quilt-mappings:1.19.1+build.2:v2"))
        officialMojangMappings()
    })

    modImplementation("net.fabricmc:fabric-loader:0.14.8")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.58.5+1.19.1")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.8.2+kotlin.1.7.10")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}