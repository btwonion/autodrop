package dev.nyon.simpleautodrop.config.models

import dev.nyon.simpleautodrop.util.ResourceLocationSerializer
import kotlinx.serialization.Serializable
import net.minecraft.resources.ResourceLocation

@Serializable
data class ConfigV3(
    var enabled: Boolean = true,
    val archives: MutableList<ArchiveV2> = mutableListOf(),
    val activeArchives: MutableList<String> = mutableListOf()
) : Config<Unit> {
    override fun transformToNew() {}
}

@Serializable
data class ArchiveV2(
    val name: String,
    var items: MutableList<@Serializable(with = ResourceLocationSerializer::class) ResourceLocation>,
    var lockedSlots: MutableList<Int>
)