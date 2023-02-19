package dev.nyon.simpleautodrop.config.models

import dev.nyon.simpleautodrop.config.Archive
import kotlinx.serialization.Serializable

@Serializable
data class ConfigV2(
    var enabled: Boolean = true,
    val archives: MutableList<Archive> = mutableListOf(),
    val activeArchives: MutableList<String> = mutableListOf()
) : Config<Unit> {

    override fun transformToNew() {}
}