package dev.nyon.autodrop.extensions

import net.minecraft.resources.Identifier

fun identifier(location: String): Identifier? {
    return Identifier.tryParse(location)
}