package dev.nyon.autodrop.extensions

typealias ResourceLocation = /*? if >=1.21.11 {*/ net.minecraft.resources.Identifier /*?} else {*/ /*net.minecraft.resources.ResourceLocation *//*?}*/

fun resourceLocation(location: String): ResourceLocation? {
    return ResourceLocation.tryParse(location)
}