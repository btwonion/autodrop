package dev.nyon.autodrop.config

import dev.nyon.autodrop.extensions.resourceLocation
import kotlinx.serialization.json.*
import net.minecraft.core.registries.BuiltInRegistries
import kotlin.jvm.optionals.getOrNull

var config: Config = Config()
var currentItems = mutableSetOf<ArchiveEntry>()
var ignoredSlots = mutableSetOf<Int>()

fun reloadArchiveProperties() {
    currentItems.clear()
    ignoredSlots.clear()
    config.archives.filter(Archive::enabled).forEach { archive ->
        currentItems += archive.entries
        ignoredSlots += archive.ignoredSlots
    }
}

internal fun migrate(
    json: Json, jsonTree: JsonElement, version: Int?
): Config? {
    val jsonObject = jsonTree.jsonObject
    return when (version) {
        null, 1 -> Config(
            jsonObject["enabled"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: return null,
            TriggerConfig(),
            jsonObject["archives"]?.jsonArray?.map { element ->
                val archiveObject = element.jsonObject
                return@map Archive(
                    true,
                    archiveObject["name"]?.jsonPrimitive?.content ?: return null,
                    archiveObject["items"]?.jsonArray?.map secMap@{ content ->
                        val resourceLocation = resourceLocation(content.jsonPrimitive.contentOrNull ?: return null)
                        return@secMap ArchiveEntry(
                            resourceLocation?.let { BuiltInRegistries.ITEM.get(it)/*? if >=1.21.2 {*/.getOrNull()?.value()/*?}*/ }, "", 1, true
                        )
                    }?.toMutableList() ?: return null,
                    archiveObject["lockedSlots"]?.jsonArray?.map secMap@{ content ->
                        content.jsonPrimitive.content.toIntOrNull() ?: return null
                    }?.toMutableSet() ?: return null
                )
            }?.toMutableList() ?: return null,
            jsonObject["dropDelay"]?.jsonPrimitive?.longOrNull ?: return null
        )
        2 -> Config(
            jsonObject["enabled"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: return null,
            json.decodeFromJsonElement(TriggerConfig.serializer(), jsonObject["triggerConfig"] ?: return null),
            jsonObject["archives"]?.jsonArray?.map { json.decodeFromJsonElement(Archive.serializer(), it) }?.toMutableList() ?: return null,
            jsonObject["dropDelay"]?.jsonPrimitive?.longOrNull ?: return null
        )
        else -> null
    }
}
