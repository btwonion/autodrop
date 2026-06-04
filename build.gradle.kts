@file:Suppress("SpellCheckingInspection", "UnstableApiUsage", "RedundantNullableReturnType")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlinx.serialization)

    alias(libs.plugins.modstitch)

    alias(libs.plugins.mod.publish)

    `maven-publish`
}

val isFabric = modstitch.isLoom
val loader = if (isFabric) "fabric" else "neoforge"

val beta: Int = property("mod.beta").toString().toInt()
val majorVersion: String = property("mod.major-version").toString()
val mcVersion = property("vers.mcVersion").toString()
version = "$majorVersion${if (beta != 0) "-beta$beta" else ""}-$mcVersion+$loader" // Pattern is '1.0.0-beta1-1.20.6-pre.2+fabric'

val flk: String = "${libs.versions.fabric.language.kotlin.orNull}${libs.versions.kotlin.orNull}"
val fabricLoader = libs.versions.fabric.loader.get()
modstitch {
    minecraftVersion = mcVersion

    metadata {
        fun prop(property: String, block: (String) -> Unit) {
            prop(property, ifNull = {""}) { block(it) }
        }

        prop("mod.id") { modId = it }
        prop("mod.name") { modName = it }
        prop("mod.description") { modDescription = it }
        prop("mod.group") { modGroup = it }

        modVersion = project.version.toString()
        modLicense = "GNU General Public License v3.0"
        modAuthor = "btwonion"

        prop("mod.repo") { replacementProperties.put("repo", it) }
        prop("mod.icon") { replacementProperties.put("icon", it) }
        prop("vers.mcVersionRange") { replacementProperties.put("mc", it) }
        replacementProperties.put("fabric_loader", fabricLoader)
        replacementProperties.put("flk", flk)
        prop("vers.deps.fapi") { replacementProperties.put("fapi", it) }
        prop("vers.deps.yacl") { replacementProperties.put("yacl", it) }
        prop("version.deps.modmenu") { replacementProperties.put("modmenu", it) }
    }

    loom {
        fabricLoaderVersion = fabricLoader

        configureLoom {
            runConfigs.all {
                ideConfigGenerated(false)
            }
        }
    }

    moddevgradle {
        prop("vers.deps.fml") { neoForgeVersion = it }

        configureNeoForge {
            runs {
                register("mainClient") {
                    client()
                    sourceSet = sourceSets.main.get()
                    gameDirectory = layout.projectDirectory.dir("../../run")
                    environment("WAYLAND_DISPLAY", "")
                    environment("XDG_SESSION_TYPE", "x11")
                }
            }

            mods {
                register("main") {
                    sourceSet(sourceSets.main.get())
                }
            }
        }
    }

    mixin {
        addMixinsToModManifest = true

        configs.register("autodrop")
    }
}

base {
    archivesName.set(rootProject.name)
}

stonecutter {
    listOf("neoforge", "fabric").map { it to (loader == it) }
        .forEach { (name, isCurrent) -> constants[name] = isCurrent }
}

repositories {
    mavenCentral()
    maven("https://maven.terraformersmc.com")
    maven("https://repo.nyon.dev/releases")
    maven("https://maven.isxander.dev/releases")
    maven("https://maven.neoforged.net/releases/")
    // Has to be removed when NeoForge 26.2 is stable
    maven("https://prmaven.neoforged.net/NeoForge/pr3198") {
        content {
            includeModule("net.neoforged", "neoforge")
        }
    }
}

val fabricLanguageKotlin: String = "${libs.versions.fabric.language.kotlin.orNull}${libs.versions.kotlin.orNull}"
dependencies {
    fun modDependency(
        artifact: String,
        compileOnly: Boolean = false,
        api: Boolean = false
    ) {
        val configuration = if (api) {
            if (compileOnly) "modstitchModCompileOnly" else "modstitchModApi"
        } else {
            if (compileOnly) "modstitchModCompileOnlyApi" else "modstitchModImplementation"
        }

        configuration(artifact)
    }

    fun propModDependency(
        id: String,
        artifactGetter: (String) -> String,
        compileOnly: Boolean = false,
        api: Boolean = false
    ) {
        prop("vers.deps.$id") { modVersion ->
            modDependency(
                artifactGetter(modVersion),
                compileOnly,
                api
            )
        }
    }

    if (isFabric) {
        propModDependency("fapi", { "net.fabricmc.fabric-api:fabric-api:$it" }, api = true)
        modDependency("net.fabricmc:fabric-language-kotlin:$fabricLanguageKotlin")
        propModDependency("modMenu", { "com.terraformersmc:modmenu:$it" })
    } else {
        propModDependency("klf", { "dev.nyon:KotlinLangForge:2.11.2-k${libs.versions.kotlin.orNull}-$it+neoforge" }, api = true)
    }

    propModDependency("yacl", { "dev.isxander:yet-another-config-lib:$it" })

    modstitchApi(libs.konfig)
    modstitchJiJ(libs.konfig)
}

tasks {
    register("releaseMod") {
        group = "publishing"

        dependsOn("publishMods")
        dependsOn("publish")
    }

    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget = modstitch.javaVersion.map { JvmTarget.fromTarget(it.toString()) }
        }

        dependsOn("stonecutterGenerate")
    }
}

val changelogText = buildString {
    append("# v${project.version}\n")
    if (beta != 0) appendLine("### As this is still a beta version, this version can contain bugs. Feel free to report ANY misbehaviours and errors!")
    rootDir.resolve("changelog.md").readText().also(::append)
}

val supportedMcVersions: List<String> =
    property("vers.supportedMcVersions")!!.toString().split(',').map(String::trim).filter(String::isNotEmpty)

publishMods {
    displayName = "v${project.version}"
    file = modstitch.finalJarTask.flatMap { it.archiveFile }
    changelog = changelogText
    type = if (beta != 0) BETA else STABLE
    if (isFabric) modLoaders.addAll("fabric", "quilt")
    else modLoaders.add("neoforge")

    modrinth {
        projectId = "lg17V3i3"
        accessToken = providers.environmentVariable("MODRINTH_API_KEY")
        minecraftVersions.addAll(supportedMcVersions)

        if (isFabric) {
            requires { slug = "fabric-api" }
            requires { slug = "fabric-language-kotlin" }
            optional { slug = "modmenu" }
        } else {
            requires { slug = "kotlin-lang-forge" }
        }

        requires { slug = "yacl" }
    }

    curseforge {
        projectId = "1244691"
        accessToken = providers.environmentVariable("CURSEFORGE_API_KEY")
        minecraftVersions.addAll(supportedMcVersions.map { if (it.contains('-')) "${it.substringBefore('-')}-Snapshot" else it })

        if (isFabric) {
            requires { slug = "fabric-api" }
            requires { slug = "fabric-language-kotlin" }
            optional { slug = "modmenu" }
        } else {
            requires { slug = "kotlinlangforge" }
        }

        requires { slug = "yacl" }
    }

    github {
        repository = property("mod.repo").toString()
        accessToken = providers.environmentVariable("GITHUB_TOKEN")
        commitish = property("mod.main-branch").toString()
    }
}

publishing {
    repositories {
        maven {
            name = "nyon"
            url = uri("https://repo.nyon.dev/releases")
            credentials {
                username = providers.environmentVariable("NYON_USERNAME").orNull
                password = providers.environmentVariable("NYON_PASSWORD").orNull
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "dev.nyon"
            artifactId = property("mod.name").toString()
            version = project.version.toString()
            from(components["java"])
        }
    }
}

java {
    withSourcesJar()
}

fun <T> prop(property: String, required: Boolean = false, ifNull: () -> String? = { null }, block: (String) -> T?): T? {
    return ((System.getenv(property) ?: findProperty(property)?.toString())
        ?.takeUnless { it.isBlank() }
        ?: ifNull())
        .let { if (required && it == null) error("Property $property is required") else it }
        ?.let(block)
}
