package dev.nyon.autodrop.screen

import dev.nyon.autodrop.config.models.Archive
import dev.nyon.autodrop.config.reloadArchiveProperties
import dev.nyon.autodrop.config.settings
import dev.nyon.autodrop.util.button
import dev.nyon.konfig.config.saveConfig
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class CreateArchiveScreen(private val previous: Screen, private val configScreen: ConfigScreen) :
    Screen(Component.translatable("menu.autodrop.createarchive.name")) {

    private lateinit var nameInput: EditBox
    private lateinit var nameInputSuccess: Button

    override fun render(matrices: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        renderDirtBackground(matrices)
        matrices.drawCenteredString(
            Minecraft.getInstance().font,
            Component.translatable("menu.autodrop.createarchive.entername"),
            this.width / 2,
            this.height / 8,
            0x80FFFFFF.toInt()
        )
        super.render(matrices, mouseX, mouseY, delta)
    }

    override fun init() {
        initWidgets()

        nameInput.value = "Archive ${settings.archives.size}"
        nameInput.setResponder {
            nameInputSuccess.active = settings.archives.find { archive -> archive.name == it } == null
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
            Component.translatable("menu.autodrop.createarchive.enternewname")
        )
        nameInputSuccess = button(
            (this.width / 2) - (this.width / 8),
            (this.height / 8) * 5,
            this.width / 4,
            20,
            Component.translatable("menu.autodrop.createarchive.confirm")
        ) {
            if (!it.isActive) return@button
            val newArchive = nameInput.value
            val archive = Archive(newArchive, mutableListOf(), mutableListOf())
            settings.archives += archive
            settings.activeArchives += archive.name
            reloadArchiveProperties()
            saveConfig(settings)
            onClose()
            configScreen.archiveListWidget.refreshEntries()
        }
    }
}