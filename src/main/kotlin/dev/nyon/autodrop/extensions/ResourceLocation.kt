package dev.nyon.autodrop.extensions

import net.minecraft.resources.ResourceLocation

fun resourceLocation(location: String): ResourceLocation? {
    return ResourceLocation.tryParse(location)
}