package dev.nyon.autodrop.extensions

import net.minecraft.resources.ResourceLocation

fun resourceLocation(location: String): ResourceLocation {
    //? if >=1.20.6
    return ResourceLocation.tryParse(location)!!

    //? if <1.20.6
    /*return ResourceLocation(location)*/
}