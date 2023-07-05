@file:Suppress("SpellCheckingInspection")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.22"
    kotlin("plugin.serialization") version "1.8.22"
    id("fabric-loom") version "1.3-SNAPSHOT"
    id("io.github.juuxel.loom-quiltflower") version "1.9.0"

    id("com.modrinth.minotaur") version "2.8.1"
    id("com.github.breadmoirai.github-release") version "2.4.1"

    `maven-publish`
}

group = "dev.nyon"
val majorVersion = "1.6.0"
val mcVersion = "1.20"
version = "$majorVersion-$mcVersion"
val authors = listOf("btwonion")
val githubRepo = "btwonion/autodrop"

repositories {
    mavenCentral()
    maven("https://maven.terraformersmc.com")
    maven("https://maven.parchmentmc.org")
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.layered {
        parchment("org.parchmentmc.data:parchment-1.20.1:2023.07.02@zip")
        officialMojangMappings()
    })
    modImplementation("net.fabricmc:fabric-loader:0.14.21")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.83.0+$mcVersion")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.9.6+kotlin.1.8.22")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    modApi("com.terraformersmc:modmenu:7.0.0")
}

tasks {
    processResources {
        val modId = "autodrop"
        val modName = "autodrop"
        val modDescription = "Mod to automatically drop items from your inventory"

        inputs.property("id", modId)
        inputs.property("name", modName)
        inputs.property("description", modDescription)
        inputs.property("version", project.version)
        inputs.property("github", githubRepo)

        filesMatching("fabric.mod.json") {
            expand(
                "id" to modId,
                "name" to modName,
                "description" to modDescription,
                "version" to project.version,
                "github" to githubRepo,
            )
        }
    }

    register("releaseMod") {
        group = "publishing"

        dependsOn("modrinthSyncBody")
        dependsOn("modrinth")
        dependsOn("githubRelease")
        dependsOn("publish")
    }
}

val majorVersionText = file("changelogs/$majorVersion.md").takeIf { it.exists() }?.readText() ?: error("No changelog provided!")
val changelogText = buildString {
    append(majorVersionText)
    file("changelogs/$version.md").takeIf { it.exists() }?.readText()?.also { append(it) }
}

modrinth {
    token.set(findProperty("modrinth.token")?.toString())
    projectId.set("lg17V3i3")
    versionNumber.set("${project.version}")
    versionType.set("release")
    uploadFile.set(tasks["remapJar"])
    gameVersions.set(listOf(mcVersion))
    loaders.set(listOf("fabric", "quilt"))
    dependencies {
        required.project("fabric-api")
        required.project("fabric-language-kotlin")
        optional.project("modmenu")
    }
    changelog.set(changelogText)
    syncBodyFrom.set(file("README.md").readText())
}

githubRelease {
    token(findProperty("github.token")?.toString())

    val split = githubRepo.split("/")
    owner(split[0])
    repo(split[1])
    tagName("v${project.version}")
    body(changelogText)
    overwrite(true)
    releaseAssets(tasks["remapJar"].outputs.files)
    targetCommitish("main")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
    kotlinOptions.freeCompilerArgs += "-Xskip-prerelease-check"
}

publishing {
    repositories {
        maven {
            name = "nyon"
            url = uri("https://repo.nyon.dev/releases")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "dev.nyon"
            artifactId = "autodrop"
            version = project.version.toString()
            from(components["java"])
        }
    }
}

java {
    withSourcesJar()
}