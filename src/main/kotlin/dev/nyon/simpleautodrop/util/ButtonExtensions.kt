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
    onClick: (Button) -> Unit,
    onTooltip: (button: Button, poseStack: PoseStack, i: Int, j: Int) -> Unit = { _, _, _, _ -> }
) = object : Button(i, j, k, l, name, onClick, onTooltip, { Component.empty() }) {}