package dev.nyon.autodrop.extensions

import net.minecraft.network.chat.Component

fun screenComponent(key: String): Component {
    return Component.translatable("menu.autodrop.screen.$key")
}