package dev.nyon.autodrop.extensions

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

fun screenComponent(key: String, vararg objects: Any): MutableComponent {
    return Component.translatable("menu.autodrop.screen.$key", *objects)
}