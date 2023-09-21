package dev.nyon.autodrop.screen

import com.mojang.blaze3d.systems.RenderSystem
import dev.nyon.autodrop.config.models.Archive
import dev.nyon.autodrop.config.reloadArchiveProperties
import dev.nyon.autodrop.config.saveConfig
import dev.nyon.autodrop.util.button
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class SetLockedSlotsScreen(private val previous: Screen, private val archive: Archive) :
    Screen(Component.translatable("menu.autodrop.lockedslots.name")) {

    private lateinit var nameInput: EditBox
    private lateinit var nameInputSuccess: Button

    private val imageWidth = 253
    private val imageHeight = 256
    override fun render(matrices: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        renderDirtBackground(matrices)

        matrices.drawCenteredString(
            Minecraft.getInstance().font,
            Component.translatable("menu.autodrop.lockedslots.name"),
            this.width / 2,
            this.height / 8,
            0x80FFFFFF.toInt()
        )

        val location = ResourceLocation("autodrop", "image/inventory-slots.png")

        RenderSystem.setShader { GameRenderer.getPositionTexShader() }
        RenderSystem.setShaderTexture(0, location)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.enableDepthTest()
        matrices.blit(location, this.width / 2 - imageWidth / 2, this.height / 4 + 75, 0, 0, imageWidth, imageHeight)
        super.render(matrices, mouseX, mouseY, delta)
    }

    override fun init() {
        initWidgets()

        nameInput.value = archive.lockedSlots.joinToString(separator = ",") {
            it.toString()
        }
        nameInput.setResponder {
            val list = it.split(',').toMutableList().also { list ->
                list.removeIf { s -> s.isEmpty() }
            }
            nameInputSuccess.active = list.none { s -> s.toIntOrNull() == null }
        }
        addRenderableWidget(nameInput)
        addRenderableWidget(nameInputSuccess)
    }

    override fun onClose() {
        minecraft?.setScreen(previous)
    }

    private fun initWidgets() {
        nameInput = EditBox(
            Minecraft.getInstance().font,
            (this.width / 2) - (this.width / 8),
            this.height / 4,
            this.width / 4,
            20,
            Component.translatable("menu.autodrop.lockedslots.enterslots")
        )

        nameInputSuccess = button(
            (this.width / 2) - (this.width / 8),
            this.height / 4 + 35,
            this.width / 4,
            20,
            Component.translatable("menu.autodrop.lockedslots.confirm")
        ) {
            if (!it.isActive) return@button
            val numbers = if (nameInput.value == "") listOf() else nameInput.value.split(',')
            archive.lockedSlots =
                if (numbers.isEmpty()) mutableListOf() else numbers.map { number -> number.toInt() }.toMutableList()
            reloadArchiveProperties()
            saveConfig()
            onClose()
        }
    }
}