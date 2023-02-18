package dev.nyon.simpleautodrop.config

import dev.nyon.simpleautodrop.config.models.Config
import dev.nyon.simpleautodrop.config.models.ConfigV1
import dev.nyon.simpleautodrop.config.models.ConfigV2
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.item.Item
import java.io.File

private val config = run {
    val file = File(FabricLoader.getInstance().configDir.toAbsolutePath().toString(), "simpleautodrop.json")
    file.createNewFile()
    return@run file
}
private val json = Json { prettyPrint = true }

private val configs: List<Config<*>> = listOf(ConfigV2(), ConfigV1())
var settings: ConfigV2 = ConfigV2()
var itemIds = mutableListOf<Int>()

fun reloadCachedIds() {
    itemIds.clear()
    settings.activeArchives.forEach { archive ->
        settings.archives.first { it.name == archive }.items.forEach {
            itemIds += Item.getId(
                it
            )
        }
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
        val requiredClass = configs.first()::class
        val oldestClass = configs.last()::class
        configs.forEach {
            try {
                val config = json.decodeFromString(it::class.serializer(), configText)
                if (config::class == requiredClass) settings = config as ConfigV2
            } catch (e: Exception) {
                val new = it.transformToNew()
                    ?: error("Something went wrong while deserializing -> Please report this to the developers of SimpleAutoDrop!")
                if (new::class == requiredClass) settings = new as ConfigV2
                if (new::class == oldestClass) saveConfig()
            }
        }
    }
}