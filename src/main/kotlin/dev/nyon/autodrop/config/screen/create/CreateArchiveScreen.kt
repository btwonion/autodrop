package dev.nyon.autodrop.config.screen.create

import dev.nyon.autodrop.config.Archive
import dev.nyon.autodrop.config.config
import dev.nyon.autodrop.config.screen.root.INNER_PAD
import dev.nyon.autodrop.config.screen.root.OUTER_PAD
import dev.nyon.autodrop.extensions.screenComponent
import dev.nyon.konfig.config.saveConfig
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import dev.nyon.autodrop.minecraft as internalMinecraft

class CreateArchiveScreen(private val parent: Screen?, private val onClose: (Archive) -> Unit) :
    Screen(screenComponent("create.title")) {
    private val matcher: (String) -> Boolean = { input ->
        input.isNotBlank() && config.archives.none { archive -> archive.name == input }
    }

    private val archiveNameEditBox =
        EditBox(internalMinecraft.font, 0, 0, 20, 20, screenComponent("create.empty")).also {
            it.onClick(10.0, 10.0)
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

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, tickDelta: Float) {
        archiveNameEditBox.setPosition(
            internalMinecraft.screen!!.width / 4,
            OUTER_PAD + INNER_PAD + internalMinecraft.font.lineHeight
        )
        archiveNameEditBox.width = internalMinecraft.screen!!.width / 2

        doneButton.setPosition(
            internalMinecraft.screen!!.width / 3,
            internalMinecraft.screen!!.height - OUTER_PAD - doneButton.height
        )
        doneButton.width = internalMinecraft.screen!!.width / 3
        doneButton.active = matcher(archiveNameEditBox.value)

        super.render(guiGraphics, mouseX, mouseY, tickDelta)

        // render description
        guiGraphics.drawCenteredString(
            internalMinecraft.font,
            screenComponent("create.description"),
            internalMinecraft.screen!!.width / 2,
            OUTER_PAD,
            0xFFFFFF
        )
    }

    override fun shouldCloseOnEsc(): Boolean {
        return matcher(archiveNameEditBox.value)
    }

    override fun onClose() {
        val archive = Archive(
            true, archiveNameEditBox.value, mutableListOf(), mutableListOf()
        )
        config.archives.add(archive)
        onClose(archive)

        internalMinecraft.setScreen(parent)
        saveConfig(config)
    }
}