package dev.nyon.autodrop.config

import kotlinx.serialization.json.*
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation

var config: Config = Config()
var currentItems = mutableSetOf<ItemIdentificator>()
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
                        val resourceLocation = /*? if >=1.21 {*/ ResourceLocation.parse(content.jsonPrimitive.contentOrNull ?: return null) /*?} else {*//* ResourceLocation(content.jsonPrimitive.contentOrNull ?: return null) *//*?}*/
                        return@secMap ItemIdentificator(
                            BuiltInRegistries.ITEM.get(resourceLocation),
                            DataComponentPatch.EMPTY,
                            1
                        )
                    }?.toMutableList() ?: return null,
                    archiveObject["lockedSlots"]?.jsonArray?.map secMap@{ content ->
                        content.jsonPrimitive.content.toIntOrNull() ?: return null
                    }?.toMutableList() ?: return null
                )
            }?.toMutableList() ?: return null,
            jsonObject["dropDelay"]?.jsonPrimitive?.longOrNull ?: return null
        )

        else -> null
    }
}
