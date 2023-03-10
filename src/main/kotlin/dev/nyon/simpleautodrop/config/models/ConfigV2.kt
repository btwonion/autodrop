package dev.nyon.simpleautodrop.config.models

import dev.nyon.simpleautodrop.util.ItemSerializer
import kotlinx.serialization.Serializable
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.Item

@Serializable
data class ConfigV2(
    var enabled: Boolean = true,
    val archives: MutableList<Archive> = mutableListOf(),
    val activeArchives: MutableList<String> = mutableListOf()
) : Config<ConfigV3> {

    override fun transformToNew(): ConfigV3 {
        return ConfigV3(
            enabled,
            archives.map { old ->
                val itemLocations = old.items.map { BuiltInRegistries.ITEM.getKey(it) }
                ArchiveV2(old.name, itemLocations.toMutableList(), old.lockedSlots) }.toMutableList(),
            activeArchives
        )
    }
}

@Serializable
data class Archive(
    val name: String,
    var items: MutableList<@Serializable(with = ItemSerializer::class) Item>,
    var lockedSlots: MutableList<Int>
)