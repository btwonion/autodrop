package dev.nyon.simpleautodrop.config.models

import dev.nyon.simpleautodrop.config.Archive

data class ConfigV2(
    var enabled: Boolean = true,
    val archives: MutableList<Archive> = mutableListOf(),
    val activeArchives: MutableList<String> = mutableListOf()
) : Config<Unit> {

    override fun transformToNew() {}
}