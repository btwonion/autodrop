package dev.nyon.autodrop.config

import dev.nyon.autodrop.extensions.emptyStoredComponents
import dev.nyon.autodrop.extensions.resourceLocation
import kotlinx.serialization.json.*
import net.minecraft.core.registries.BuiltInRegistries
import kotlin.jvm.optionals.getOrNull

var config: Config = Config()
var currentItems = mutableSetOf<ItemIdentifier>()
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
    jsonTree: JsonElement, version: Int?
): Config? {
    val jsonObject = jsonTree.jsonObject
    return when (version) {
        null, 1 -> Config(
            jsonObject["enabled"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: return null,
            TriggerConfig(),
            jsonObject["archives"]?.jsonArray?.map {
                val archiveObject = it.jsonObject
                return@map Archive(
                    true,
                    archiveObject["name"]?.jsonPrimitive?.content ?: return null,
                    archiveObject["items"]?.jsonArray?.map secMap@{ content ->
                        val resourceLocation = resourceLocation(content.jsonPrimitive.contentOrNull ?: return null)
                        return@secMap ItemIdentifier(
                            resourceLocation?.let { BuiltInRegistries.ITEM.get(it)/*? if >=1.21.2 {*//*.getOrNull()?.value()*//*?}*/ }, emptyStoredComponents, 1
                        )
                    }?.toMutableList() ?: return null,
                    archiveObject["lockedSlots"]?.jsonArray?.map secMap@{ content ->
                        content.jsonPrimitive.content.toIntOrNull() ?: return null
                    }?.toMutableSet() ?: return null
                )
            }?.toMutableList() ?: return null,
            jsonObject["dropDelay"]?.jsonPrimitive?.longOrNull ?: return null
        )

        else -> null
    }
}
