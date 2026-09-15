package dev.nyon.autodrop.extensions

import net.minecraft.client.KeyMapping

private val KEY_BINDING_CATEGORY: KeyMapping.Category = KeyMapping.Category(identifier("autodrop:main")!!)

fun keyMapping(location: String, key: Int): KeyMapping {
    return KeyMapping(location, key, KEY_BINDING_CATEGORY)
}
