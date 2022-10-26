package dev.nyon.simpleautodrop.screen

import com.mojang.blaze3d.vertex.PoseStack
import dev.nyon.simpleautodrop.config.reloadCachedIds
import dev.nyon.simpleautodrop.config.saveConfig
import dev.nyon.simpleautodrop.config.settings
import dev.nyon.simpleautodrop.util.button
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiComponent
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class CreateArchiveScreen(private val previous: Screen, private val configScreen: ConfigScreen) :
    Screen(Component.literal("Create archive")) {

    private lateinit var nameInput: EditBox
    private lateinit var nameInputSuccess: Button

    override fun render(matrices: PoseStack, mouseX: Int, mouseY: Int, delta: Float) {
        renderDirtBackground(1)
        GuiComponent.drawCenteredString(
            matrices,
            Minecraft.getInstance().font,
            Component.literal("Enter archive name"),
            this.width / 2,
            this.height / 8,
            0x80FFFFFF.toInt()
        )
        super.render(matrices, mouseX, mouseY, delta)
    }

    override fun init() {
        initWidgets()

        nameInput.value = "Archive ${settings.items.size}"
        nameInput.setResponder {
            nameInputSuccess.active = !settings.items.containsKey(it)
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
            Component.literal("Enter new archive name here...")
        )
        nameInputSuccess = button((this.width / 2) - (this.width / 8),
            (this.height / 8) * 5,
            this.width / 4,
            20,
            Component.literal("Confirm"),
            {
                if (!it.isActive) return@button
                val newArchive = nameInput.value
                settings.items[newArchive] = mutableListOf()
                reloadCachedIds()
                saveConfig()
                onClose()
                configScreen.archiveListWidget.refreshEntries()
            })
    }
}