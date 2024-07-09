package dev.nyon.autodrop.config.screen

import dev.nyon.autodrop.config.Archive
import dev.nyon.autodrop.config.config
import dev.nyon.autodrop.extensions.screenComponent
import dev.nyon.konfig.config.saveConfig
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import dev.nyon.autodrop.minecraft as internalMinecraft

const val INNER_PAD = 5
const val OUTER_PAD = 10

class ArchiveScreen(private val parent: Screen?) : Screen(screenComponent("title")) {
    var selected: Archive = config.archives.first()

    private val archivesWidget = ArchivesWidget(this).also {
        addWidget(it)
        it.refreshEntries()
    }

    private val archiveItemsWidget = ArchiveItemsWidget(selected).also {
        addWidget(it)
        it.refreshEntries()
    }

    private val doneButton = Button.builder(screenComponent("done")) {
        onClose()
    }.width(internalMinecraft.screen!!.width / 4 - 2 * OUTER_PAD).build().also { addWidget(it) }

    private val setIgnoredSlotsButton = Button.builder(screenComponent("ignored")) {
        // TODO: open ignored slots screen
    }.width(internalMinecraft.screen!!.width / 4 - 2 * OUTER_PAD).build().also { addWidget(it) }

    private val createArchiveButton = Button.builder(screenComponent("create")) {
        // TODO: open create archive screen
    }.width(internalMinecraft.screen!!.width / 4 - 2 * OUTER_PAD).build().also { addWidget(it) }

    private val deleteArchiveButton = Button.builder(screenComponent("delete").withStyle(ChatFormatting.DARK_RED)) {
        config.archives.remove(selected)
        selected = config.archives.first()
        archivesWidget.refreshEntries()
    }.width(internalMinecraft.screen!!.width / 4 - 2 * OUTER_PAD).build().also { addWidget(it) }

    override fun onClose() {
        minecraft!!.setScreen(parent)
        saveConfig(config)
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, tickDelta: Float) {
        renderBackground(guiGraphics, mouseX, mouseY, tickDelta)
        archivesWidget.render(guiGraphics, mouseX, mouseY, tickDelta)

        // render archive item list, if empty, render info
        if (selected.entries.isEmpty()) guiGraphics.drawCenteredString(
            internalMinecraft.font,
            screenComponent("noitems"),
            internalMinecraft.screen!!.width / 8 * 5,
            internalMinecraft.screen!!.height / 3,
            0xFFFFFF
        )
        else archiveItemsWidget.render(guiGraphics, mouseX, mouseY, tickDelta)

        // render control buttons
        doneButton.setPosition(OUTER_PAD, internalMinecraft.screen!!.height - OUTER_PAD - 20)
        doneButton.width = internalMinecraft.screen!!.width / 4 - 2 * OUTER_PAD
        doneButton.render(guiGraphics, mouseX, mouseY, tickDelta)

        setIgnoredSlotsButton.setPosition(OUTER_PAD, internalMinecraft.screen!!.height - OUTER_PAD - 2 * 20 - 3)
        setIgnoredSlotsButton.width = internalMinecraft.screen!!.width / 4 - 2 * OUTER_PAD
        setIgnoredSlotsButton.render(guiGraphics, mouseX, mouseY, tickDelta)

        createArchiveButton.setPosition(OUTER_PAD, internalMinecraft.screen!!.height - OUTER_PAD - 3 * 20 - 6)
        createArchiveButton.width = internalMinecraft.screen!!.width / 4 - 2 * OUTER_PAD
        createArchiveButton.render(guiGraphics, mouseX, mouseY, tickDelta)

        deleteArchiveButton.setPosition(OUTER_PAD, internalMinecraft.screen!!.height - OUTER_PAD - 4 * 20 - 9)
        deleteArchiveButton.width = internalMinecraft.screen!!.width / 4 - 2 * OUTER_PAD
        deleteArchiveButton.render(guiGraphics, mouseX, mouseY, tickDelta)
    }

    fun select(archive: Archive) {
        selected = archive
        archiveItemsWidget.archive = archive
        archiveItemsWidget.refreshEntries()
    }
}