package dev.nyon.simpleautodrop.screen

import com.mojang.blaze3d.vertex.PoseStack
import dev.nyon.simpleautodrop.config.itemIds
import dev.nyon.simpleautodrop.config.saveConfig
import dev.nyon.simpleautodrop.config.settings
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiComponent
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.silkmc.silk.core.text.literalText

class CreateArchiveScreen(private val previous: Screen, private val configScreen: ConfigScreen) :
    Screen(literalText("Create archive")) {

    private val nameInput = EditBox(
        Minecraft.getInstance().font, 380, 170, 200, 20, literalText("Enter new archive name here...")
    )
    private val nameInputSuccess = Button(380, 205, 200, 20, literalText("Confirm")) {
        if (!it.isActive) return@Button
        val newArchive = nameInput.value
        settings.items[newArchive] = mutableListOf()
        itemIds[newArchive] = mutableListOf()
        saveConfig()
        onClose()
        configScreen.archiveListWidget.refreshEntries()
    }

    override fun render(matrices: PoseStack, mouseX: Int, mouseY: Int, delta: Float) {
        renderDirtBackground(1)
        GuiComponent.drawCenteredString(
            matrices, Minecraft.getInstance().font, literalText("Enter archive name"), 480, 100, 0x80FFFFFF.toInt()
        )
        super.render(matrices, mouseX, mouseY, delta)
    }

    override fun init() {
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

}