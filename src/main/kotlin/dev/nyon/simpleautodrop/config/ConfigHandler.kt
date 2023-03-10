package dev.nyon.simpleautodrop.config

import dev.nyon.simpleautodrop.config.models.Config
import dev.nyon.simpleautodrop.config.models.ConfigV1
import dev.nyon.simpleautodrop.config.models.ConfigV2
import dev.nyon.simpleautodrop.config.models.ConfigV3
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import net.fabricmc.loader.api.FabricLoader
import java.io.File

private val config = run {
    val file = File(FabricLoader.getInstance().configDir.toAbsolutePath().toString(), "simpleautodrop.json")
    file.createNewFile()
    return@run file
}
private val json = Json {
    prettyPrint = true
    encodeDefaults = true
}

private val configs: List<Config<*>> = listOf(ConfigV3(), ConfigV2(), ConfigV1())
var settings: ConfigV3 = ConfigV3()
var itemIds = mutableListOf<String>()
var blockedSlots = mutableListOf<Int>()

fun reloadArchiveProperties() {
    itemIds.clear()
    blockedSlots.clear()
    settings.activeArchives.forEach { archiveName ->
        val archive = settings.archives.first { it.name == archiveName }
        itemIds += archive.items.map { it.toString() }
        blockedSlots += archive.lockedSlots
    }
}

fun saveConfig() {
    config.writeText(json.encodeToString(settings))
}

@OptIn(InternalSerializationApi::class)
fun loadConfig() {
    if (config.readText().isEmpty()) saveConfig()
    else {
        val configText = config.readText()
        val requiredClass = ConfigV3::class
        try {
            settings = json.decodeFromString(configText)
        } catch (e: Exception) {
            configs.drop(1).forEach {
                try {
                    val config = json.decodeFromString(it::class.serializer(), configText)
                    if (config::class == requiredClass) settings = config as ConfigV3
                    else throw Exception()
                } catch (e: Exception) {
                    val new = it.transformToNew()
                        ?: error("Something went wrong while deserializing -> Please report this to the developers of SimpleAutoDrop!")
                    if (new::class == requiredClass) {
                        settings = new as ConfigV3
                        config.writeText(json.encodeToString(new))
                        return@forEach
                    }
                    if (new::class == Unit::class) saveConfig()
                }
            }
        }
    }
}