package dev.nyon.autodrop.extensions

import dev.nyon.autodrop.AutoDrop
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.input.MouseButtonInfo

fun EditBox.select(x: Double, y: Double) {
    onClick(MouseButtonEvent(x, y, MouseButtonInfo(1, 0)), false)
}

val screenWidth: Int
    get() = AutoDrop.minecraft.window.guiScaledWidth
val screenHeight: Int
    get() = AutoDrop.minecraft.window.guiScaledHeight