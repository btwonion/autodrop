package dev.nyon.autodrop.config.models

import dev.nyon.autodrop.util.IdentifierSerializer
import kotlinx.serialization.Serializable
import net.minecraft.util.Identifier

@Serializable
data class Archive(
    val name: String,
    var items: MutableList<@Serializable(with = IdentifierSerializer::class) Identifier>,
    var lockedSlots: MutableList<Int>
)