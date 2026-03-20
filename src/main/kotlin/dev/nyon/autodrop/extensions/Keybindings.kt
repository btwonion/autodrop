package dev.nyon.autodrop.extensions

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping

private val KEY_BINDING_CATEGORY: KeyMapping.Category = KeyMapping.Category(identifier("autodrop:main")!!)

fun keyMapping(location: String, key: Int): KeyMapping {
    return KeyMapping(location, InputConstants.Type.KEYSYM, key, KEY_BINDING_CATEGORY)
}