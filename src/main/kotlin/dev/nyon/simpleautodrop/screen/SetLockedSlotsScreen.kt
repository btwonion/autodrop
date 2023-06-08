package dev.nyon.simpleautodrop.screen

import com.mojang.blaze3d.systems.RenderSystem
import dev.nyon.simpleautodrop.config.models.ArchiveV2
import dev.nyon.simpleautodrop.config.reloadArchiveProperties
import dev.nyon.simpleautodrop.config.saveConfig
import dev.nyon.simpleautodrop.util.button
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class SetLockedSlotsScreen(private val previous: Screen, private val archive: ArchiveV2) :
    Screen(Component.literal("Set locked slots")) {

    private lateinit var nameInput: EditBox
    private lateinit var nameInputSuccess: Button

    private val imageWidth = 253
    private val imageHeight = 256
    override fun render(matrices: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        renderDirtBackground(matrices)

        matrices.drawCenteredString(
            Minecraft.getInstance().font,
            Component.literal("Set locked slots"),
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
            Component.literal("Enter slots you would like to lock (separated by commas)")
        )
        nameInputSuccess = button(
            (this.width / 2) - (this.width / 8), this.height / 4 + 35, this.width / 4, 20, Component.literal("Confirm")
        ) {
            if (!it.isActive) return@button
            archive.lockedSlots = nameInput.value.split(",").map { number -> number.toInt() }.toMutableList()
            reloadArchiveProperties()
            saveConfig()
            onClose()
        }
    }

}