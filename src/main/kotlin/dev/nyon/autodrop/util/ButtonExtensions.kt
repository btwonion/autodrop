package dev.nyon.autodrop.util

import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text

fun button(
    x: Int, y: Int, width: Int, height: Int, description: Text, onClick: (ButtonWidget) -> Unit
): ButtonWidget {
    val buttonHeight = if (height > 20) 20 else height
    val buttonWidth = if (width > 50) 50 else width
    val button = object : ButtonWidget(x, y, buttonWidth, buttonHeight, description, onClick, { Text.empty() }) {}
    return button
}