package dev.nyon.autodrop.config.screen.create

import dev.nyon.autodrop.config.Archive
import dev.nyon.autodrop.config.config
import dev.nyon.autodrop.config.screen.root.INNER_PAD
import dev.nyon.autodrop.config.screen.root.OUTER_PAD
import dev.nyon.autodrop.extensions.screenComponent
import dev.nyon.autodrop.extensions.screenHeight
import dev.nyon.autodrop.extensions.screenWidth
import dev.nyon.autodrop.extensions.select
import dev.nyon.konfig.config.saveConfig
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import dev.nyon.autodrop.AutoDrop.minecraft as internalMinecraft

class CreateArchiveScreen(private val parent: Screen?, private val onClose: (Archive) -> Unit) :
    Screen(screenComponent("create.title")) {
    private val matcher: (String) -> Boolean = { input ->
        input.isNotBlank() && config.archives.none { archive -> archive.name == input }
    }

    private val archiveNameEditBox =
        EditBox(internalMinecraft.font, 0, 0, 20, 20, screenComponent("create.empty")).also {
            it.select(10.0, 10.0)
            it.cursorPosition = 0
            it.setHighlightPos(0)
        }

    private val doneButton = Button.builder(screenComponent("done")) {
        onClose()
    }.build()

    override fun init() {
        addRenderableWidget(archiveNameEditBox)
        addRenderableWidget(doneButton)
        super.init()
    }

    override fun extractRenderState(guiGraphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, tickDelta: Float) {
        archiveNameEditBox.setPosition(
            screenWidth / 4, OUTER_PAD + INNER_PAD + internalMinecraft.font.lineHeight
        )
        archiveNameEditBox.width = screenWidth / 2

        doneButton.setPosition(
            screenWidth / 3, screenHeight - OUTER_PAD - doneButton.height
        )
        doneButton.width = screenWidth / 3
        doneButton.active = matcher(archiveNameEditBox.value)

        super.extractRenderState(guiGraphics, mouseX, mouseY, tickDelta)

        // render description
        guiGraphics.centeredText(
            internalMinecraft.font,
            screenComponent("create.description"),
            screenWidth / 2,
            OUTER_PAD,
            0xFFFFFFFF.toInt()
        )
    }

    override fun shouldCloseOnEsc(): Boolean {
        return matcher(archiveNameEditBox.value)
    }

    override fun onClose() {
        val archive = Archive(
            true, archiveNameEditBox.value, mutableListOf(), mutableSetOf()
        )
        config.archives.add(archive)
        onClose(archive)

        internalMinecraft.setScreen(parent)
        saveConfig(config)
    }
}