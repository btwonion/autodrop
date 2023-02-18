package dev.nyon.simpleautodrop.config.models

import kotlinx.serialization.Serializable

@Serializable
sealed interface Config<P> {
    fun transformToNew(): P
}