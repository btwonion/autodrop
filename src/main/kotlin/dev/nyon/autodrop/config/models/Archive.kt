package dev.nyon.autodrop.config.models

import dev.nyon.autodrop.util.ResourceLocationSerializer
import kotlinx.serialization.Serializable
import net.minecraft.resources.ResourceLocation


@Serializable
data class Archive(
    val name: String,
    var items: MutableList<@Serializable(with = ResourceLocationSerializer::class) ResourceLocation>,
    var lockedSlots: MutableList<Int>
)