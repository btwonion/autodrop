package dev.nyon.autodrop.util

import net.minecraft.client.gui.components.Button
import net.minecraft.network.chat.Component

fun button(
    x: Int, y: Int, width: Int, height: Int, description: Component, onClick: (Button) -> Unit
): Button {
    val buttonHeight = if (height > 20) 20 else height
    val buttonWidth = if (width > 50) 50 else width
    val button = object : Button(x, y, buttonWidth, buttonHeight, description, onClick, { Component.empty() }) {}
    return button
}