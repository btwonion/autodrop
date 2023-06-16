package dev.nyon.simpleautodrop.screen

import dev.nyon.simpleautodrop.config.reloadArchiveProperties
import dev.nyon.simpleautodrop.config.saveConfig
import dev.nyon.simpleautodrop.config.settings
import dev.nyon.simpleautodrop.screen.archive.ArchiveListWidget
import dev.nyon.simpleautodrop.screen.archiveEntry.ArchiveEntryListWidget
import dev.nyon.simpleautodrop.util.button
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style

class ConfigScreen(private val previousScreen: Screen?) : Screen(Component.literal("SimpleAutoDrop")) {

    var currentArchive: String = ""

    lateinit var archiveEntryListWidget: ArchiveEntryListWidget
    lateinit var addItemsToArchiveButton: Button
    lateinit var setLockedSlotsButton: Button
    lateinit var archiveListWidget: ArchiveListWidget
    lateinit var deleteButton: Button
    private lateinit var createArchiveButton: Button
    private lateinit var doneButton: Button

    override fun init() {
        initWidgets()

        addRenderableWidget(archiveEntryListWidget)
        addRenderableWidget(archiveListWidget)
        addRenderableWidget(deleteButton)
        addRenderableWidget(doneButton)
        addRenderableWidget(createArchiveButton)
        addRenderableWidget(addItemsToArchiveButton)
        addRenderableWidget(setLockedSlotsButton)

        addItemsToArchiveButton.active = false
        setLockedSlotsButton.active = false
        deleteButton.active = false
    }

    override fun render(matrices: GuiGraphics, i: Int, j: Int, f: Float) {
        renderDirtBackground(matrices)
        super.render(matrices, i, j, f)
    }

    override fun onClose() {
        minecraft?.setScreen(previousScreen)
    }

    private fun initWidgets() {
        archiveEntryListWidget = ArchiveEntryListWidget(
            (this.width / 4) * 3 - 15,
            (this.width / 4) + 10,
            (this.height / 24) * 23 - 15,
            10,
            (this.height / 24) * 23 - 5,
            24,
            currentArchive
        )
        addItemsToArchiveButton = button(
            (this.width / 4) + 10 + archiveEntryListWidget.rowWidth / 2 + 5,
            (this.height / 24) * 23,
            this.width / 4,
            20,
            Component.literal("Add items")
        ) {
            minecraft?.setScreen(null)
            minecraft?.setScreen(AddItemsScreen(this, currentArchive, this))
        }
        setLockedSlotsButton = button(
            (this.width / 4) + 10 + (archiveEntryListWidget.rowWidth / 2) - 5 - (this.width / 4),
            (this.height / 24) * 23,
            this.width / 4,
            20,
            Component.literal("Set locked slots")
        ) {
            minecraft?.setScreen(null)
            minecraft?.setScreen(
                SetLockedSlotsScreen(this, settings.archives.first { archive -> archive.name == currentArchive })
            )
        }
        archiveListWidget =
            ArchiveListWidget(this.width / 4, 5, (this.height / 24) * 21 - 10, 10, ((this.height / 24) * 21), 24, this)
        deleteButton = button(
            5,
            (this.height / 24) * 22,
            (this.width / 8) - 2,
            20,
            Component.literal("Delete").withStyle(Style.EMPTY.withColor(0x99620401.toInt()))
        ) { button ->
            if (!button.active) return@button
            if (currentArchive == "") return@button
            settings.archives.removeIf { it.name == currentArchive }
            settings.activeArchives.removeIf { it == currentArchive }
            reloadArchiveProperties()
            saveConfig()
            currentArchive = ""
            button.active = false
            archiveEntryListWidget.refreshEntries()
            archiveListWidget.refreshEntries()
            addItemsToArchiveButton.active = false
            setLockedSlotsButton.active = false
        }
        createArchiveButton = button(
            5 + ((this.width / 8) - 2) + 2,
            (this.height / 24) * 22,
            (this.width / 8),
            20,
            Component.literal("Create archive")
        ) {
            minecraft?.setScreen(null)
            minecraft?.setScreen(CreateArchiveScreen(this, this))
        }
        doneButton = button(5, (this.height / 24) * 23, this.width / 4, 20, Component.literal("Done")) {
            saveConfig()
            minecraft?.setScreen(previousScreen)
        }
    }
}