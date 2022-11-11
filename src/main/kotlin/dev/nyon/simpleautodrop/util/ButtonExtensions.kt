package dev.nyon.simpleautodrop.util

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.components.Button
import net.minecraft.network.chat.Component

fun button(
    i: Int,
    j: Int,
    k: Int,
    l: Int,
    name: Component,
    onTooltip: (Button, PoseStack, Int, Int) -> Unit = { _, _, _, _ -> },
    onClick: (Button) -> Unit
) = object : Button(i, j, k, l, name, onClick, onTooltip) {}