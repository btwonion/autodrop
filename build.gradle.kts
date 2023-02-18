@file:Suppress("SpellCheckingInspection")
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
    id("fabric-loom") version "1.1-SNAPSHOT"
    id("io.github.juuxel.loom-quiltflower") version "1.8.0"

    id("com.modrinth.minotaur") version "2.7.2"
    id("com.github.breadmoirai.github-release") version "2.4.1"
}

group = "dev.nyon"
val majorVersion = "1.4.0"
version = "$majorVersion-1.19.3"
val authors = listOf("btwonion")
val githubRepo = "btwonion/SimpleAutoDrop"

repositories {
    mavenCentral()
    maven("https://maven.terraformersmc.com")
    maven("https://maven.parchmentmc.org")
}

dependencies {
    minecraft("com.mojang:minecraft:1.19.3")
    mappings(loom.layered {
        parchment("org.parchmentmc.data:parchment-1.19.3:2023.02.05@zip")
        officialMojangMappings()
    })
    modImplementation("net.fabricmc:fabric-loader:0.14.14")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.74.0+1.19.3")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.9.1+kotlin.1.8.10")
    modApi("com.terraformersmc:modmenu:5.0.2")
}

tasks {
    processResources {
        val modId = "autodrop"
        val modName = "SimpleAutoDrop"
        val modDescription = "Mod to automatically drop items from your inventory"

        inputs.property("id", modId)
        inputs.property("group", project.group)
        inputs.property("name", modName)
        inputs.property("description", modDescription)
        inputs.property("version", project.version)
        inputs.property("github", githubRepo)

        filesMatching("fabric.mod.json") {
            expand(
                "id" to modId,
                "group" to project.group,
                "name" to modName,
                "description" to modDescription,
                "version" to project.version,
                "github" to githubRepo,
            )
        }
    }

    register("releaseMod") {
        group = "publishing"

        dependsOn("modrinth")
        dependsOn("githubRelease")
        dependsOn("modrinthSyncBody")
    }
}
val changelogText =
    file("changelogs/$majorVersion.md").takeIf { it.exists() }?.readText() ?: "No changelog provided."

modrinth {
    token.set(findProperty("modrinth.token")?.toString())
    projectId.set("lg17V3i3")
    versionNumber.set("${project.version}")
    versionType.set("release")
    uploadFile.set(tasks["remapJar"])
    gameVersions.set(listOf("1.19.3"))
    loaders.set(listOf("fabric", "quilt"))
    dependencies {
        required.project("fabric-api")
        required.project("fabric-language-kotlin")
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