package dev.nyon.autodrop.config.screen.create

import dev.nyon.autodrop.config.Archive
import dev.nyon.autodrop.config.config
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
            addWidget(it)
            it.onClick(10.0, 10.0)
            it.setFilter(matcher)
            it.cursorPosition = 0
            it.setHighlightPos(0)
        }

    private val doneButton = Button.builder(screenComponent("done")) {
        onClose()
    }.build().also { addWidget(it) }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, tickDelta: Float) {
        renderBackground(guiGraphics, mouseX, mouseY, tickDelta)

        // render description
        guiGraphics.drawCenteredString(
            internalMinecraft.font,
            screenComponent("create.description"),
            internalMinecraft.screen!!.width / 2,
            internalMinecraft.screen!!.height / 6,
            0xFFFFFF
        )

        // render edit box
        archiveNameEditBox.setPosition(internalMinecraft.screen!!.width / 3, internalMinecraft.screen!!.height / 4)
        archiveNameEditBox.width = internalMinecraft.screen!!.width / 3
        archiveNameEditBox.render(guiGraphics, mouseX, mouseY, tickDelta)

        // render done button
        doneButton.setPosition(internalMinecraft.screen!!.width / 3, (internalMinecraft.screen!!.height * .8).toInt())
        doneButton.width = internalMinecraft.screen!!.width / 3
        doneButton.render(guiGraphics, mouseX, mouseY, tickDelta)
        doneButton.active = matcher(archiveNameEditBox.value)
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