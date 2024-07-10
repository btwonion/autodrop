package dev.nyon.autodrop.config.screen.root

import dev.nyon.autodrop.config.Archive
import dev.nyon.autodrop.config.ItemIdentifier
import dev.nyon.autodrop.config.config
import dev.nyon.autodrop.config.reloadArchiveProperties
import dev.nyon.autodrop.config.screen.create.CreateArchiveScreen
import dev.nyon.autodrop.config.screen.ignored.IgnoredSlotsScreen
import dev.nyon.autodrop.config.screen.modify.ModifyIdentifierScreen
import dev.nyon.autodrop.extensions.screenComponent
import dev.nyon.konfig.config.saveConfig
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.component.DataComponentPatch
import dev.nyon.autodrop.minecraft as internalMinecraft

const val INNER_PAD = 5
const val OUTER_PAD = 10

class ArchiveScreen(private val parent: Screen?) : Screen(screenComponent("title")) {
    var selected: Archive = config.archives.first()

    private val archivesWidget = ArchivesWidget(this).also {
        addWidget(it)
        it.refreshEntries()
    }

    val archiveItemsWidget = ArchiveItemsWidget(selected, this@ArchiveScreen).also {
        addWidget(it)
        it.refreshEntries()
    }

    private val doneButton = Button.builder(screenComponent("done")) {
        onClose()
    }.build().also { addWidget(it) }

    private val setIgnoredSlotsButton = Button.builder(screenComponent("ignored")) {
        internalMinecraft.setScreen(IgnoredSlotsScreen(selected, this@ArchiveScreen))
    }.build().also { addWidget(it) }

    private val createArchiveButton = Button.builder(screenComponent("create")) {
        internalMinecraft.setScreen(CreateArchiveScreen(this@ArchiveScreen) {
            archivesWidget.refreshEntries()
            select(it)
        })
    }.build().also { addWidget(it) }

    private val deleteArchiveButton = Button.builder(screenComponent("delete").withStyle(ChatFormatting.DARK_RED)) {
        config.archives.remove(selected)
        selected = config.archives.first()
        archivesWidget.refreshEntries()
    }.build().also { addWidget(it) }

    private val addIdentifierButton = Button.builder(screenComponent("identifier")) {
        val newIdentifier = ItemIdentifier(null, DataComponentPatch.EMPTY, 1)

        selected.entries.add(newIdentifier)
        internalMinecraft.setScreen(
            ModifyIdentifierScreen(
                this@ArchiveScreen, newIdentifier
            )
        )
    }.build().also { addWidget(it) }

    override fun onClose() {
        internalMinecraft.setScreen(parent)
        saveConfig(config)
        reloadArchiveProperties()
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

        addIdentifierButton.setPosition(OUTER_PAD, internalMinecraft.screen!!.height - OUTER_PAD - 5 * 20 - 12)
        addIdentifierButton.width = internalMinecraft.screen!!.width / 4 - 2 * OUTER_PAD
        addIdentifierButton.render(guiGraphics, mouseX, mouseY, tickDelta)
    }

    override fun rebuildWidgets() {
        super.rebuildWidgets()
        addWidget(archivesWidget)
        addWidget(archiveItemsWidget)
        addWidget(doneButton)
        addWidget(setIgnoredSlotsButton)
        addWidget(createArchiveButton)
        addWidget(deleteArchiveButton)
        addWidget(addIdentifierButton)
    }

    fun select(archive: Archive) {
        selected = archive
        archiveItemsWidget.archive = archive
        archiveItemsWidget.refreshEntries()
    }
}