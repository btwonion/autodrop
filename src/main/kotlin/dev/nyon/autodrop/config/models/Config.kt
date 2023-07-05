package dev.nyon.autodrop.config.models

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    var enabled: Boolean = true,
    val archives: MutableList<Archive> = mutableListOf(),
    val activeArchives: MutableList<String> = mutableListOf(),
    val dropDelay: Long = 200
)