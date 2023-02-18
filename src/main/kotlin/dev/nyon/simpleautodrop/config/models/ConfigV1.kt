package dev.nyon.simpleautodrop.config.models

import dev.nyon.simpleautodrop.config.Archive
import dev.nyon.simpleautodrop.util.ItemSerializer
import kotlinx.serialization.Serializable
import net.minecraft.world.item.Item

@Serializable
data class ConfigV1(
    var enabled: Boolean = true,
    var items: MutableMap<String, MutableList<@Serializable(with = ItemSerializer::class) Item>> = mutableMapOf(),
    var currentArchives: MutableList<String> = mutableListOf()
) : Config<ConfigV2> {
    override fun transformToNew(): ConfigV2 {
        return ConfigV2(
            enabled,
            items.map { (name, items) -> Archive(name, items, mutableListOf()) }.toMutableList(),
            currentArchives
        )
    }
}