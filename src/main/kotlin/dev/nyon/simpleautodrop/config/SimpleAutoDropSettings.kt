package dev.nyon.simpleautodrop.config

import dev.nyon.simpleautodrop.util.ItemSerializer
import kotlinx.serialization.Serializable
import net.minecraft.world.item.Item

var settings: SimpleAutoDropSettings = SimpleAutoDropSettings(true, hashMapOf(), mutableListOf())
var itemIds = mutableListOf<Int>()

fun reloadCachedIds() {
    itemIds.clear()
    settings.currentArchives.forEach { archive -> settings.items[archive]?.forEach { itemIds += Item.getId(it) } }
}

@Serializable
data class SimpleAutoDropSettings(
    var enabled: Boolean,
    var items: HashMap<String, MutableList<@Serializable(with = ItemSerializer::class) Item>>,
    var currentArchives: MutableList<String>
)