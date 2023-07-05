package dev.nyon.autodrop.config

import dev.nyon.autodrop.config.models.Config
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.Item
import java.io.File

private val config = run {
    val file = File(FabricLoader.getInstance().configDir.toAbsolutePath().toString(), "simpleautodrop.json")
    file.createNewFile()
    return@run file
}
private val json = Json {
    prettyPrint = true
    encodeDefaults = true
    ignoreUnknownKeys = true
}

var settings: Config = Config()
var currentItems = mutableListOf<Item>()
var blockedSlots = mutableListOf<Int>()

fun reloadArchiveProperties() {
    currentItems.clear()
    blockedSlots.clear()
    settings.activeArchives.forEach { archiveName ->
        val archive = settings.archives.first { it.name == archiveName }
        currentItems += archive.items.map { BuiltInRegistries.ITEM.get(it) }
        blockedSlots += archive.lockedSlots
    }
}

fun saveConfig() {
    config.writeText(json.encodeToString(settings))
}

fun loadConfig() {
    try {
        settings = json.decodeFromString(config.readText())
    } catch (e: Exception) {
        saveConfig()
    }
}