package dev.nyon.simpleautodrop.config

import dev.nyon.simpleautodrop.util.ItemSerializer
import kotlinx.serialization.Serializable
import net.minecraft.world.item.Item

@Serializable
data class Archive(
    val name: String,
    val items: MutableList<@Serializable(with = ItemSerializer::class) Item>,
    val lockedSlots: MutableList<Int>
)