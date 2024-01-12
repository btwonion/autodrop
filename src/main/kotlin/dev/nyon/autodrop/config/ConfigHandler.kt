package dev.nyon.autodrop.config

import dev.nyon.autodrop.config.models.Archive
import dev.nyon.autodrop.config.models.Config
import kotlinx.serialization.json.*
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item

var settings: Config = Config()
var currentItems = mutableListOf<Item>()
var blockedSlots = mutableListOf<Int>()

fun reloadArchiveProperties() {
    currentItems.clear()
    blockedSlots.clear()
    settings.activeArchives.forEach { archiveName ->
        val archive = settings.archives.first { it.name == archiveName }
        currentItems += archive.items.map { Registries.ITEM.get(it) }
        blockedSlots += archive.lockedSlots
    }
}

internal fun migrate(jsonTree: JsonElement, version: Int?): Config? {
    val jsonObject = jsonTree.jsonObject
    return when (version) {
        null -> Config(
            jsonObject["enabled"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: return null,
            jsonObject["archives"]?.jsonArray?.map {
                val archiveObject = it.jsonObject
                return@map Archive(
                    archiveObject["name"]?.jsonPrimitive?.content ?: return null,
                    archiveObject["items"]?.jsonArray?.map secMap@{ content ->
                        return@secMap ResourceLocation(
                            content.jsonPrimitive.contentOrNull ?: return null
                        )
                    }?.toMutableList() ?: return null,
                    archiveObject["lockedSlots"]?.jsonArray?.map secMap@{ content ->
                        content.jsonPrimitive.content.toIntOrNull() ?: return null
                    }?.toMutableList() ?: return null
                )
            }?.toMutableList() ?: return null,
            jsonObject["activeArchives"]?.jsonArray?.map { it.jsonPrimitive.content }?.toMutableList() ?: return null,
            jsonObject["dropDelay"]?.jsonPrimitive?.longOrNull ?: return null
        )

        else -> null
    }
}