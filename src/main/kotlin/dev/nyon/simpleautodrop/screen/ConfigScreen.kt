package dev.nyon.simpleautodrop.screen

import com.mojang.blaze3d.vertex.PoseStack
import dev.nyon.simpleautodrop.config.itemIds
import dev.nyon.simpleautodrop.config.saveConfig
import dev.nyon.simpleautodrop.config.settings
import dev.nyon.simpleautodrop.screen.archive.ArchiveListWidget
import dev.nyon.simpleautodrop.screen.archiveEntry.ArchiveEntryListWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.silkmc.silk.core.text.literalText

class ConfigScreen(private val previousScreen: Screen?) : Screen(literalText("SimpleAutoDrop")) {

    var currentArchive: String = ""

    lateinit var archiveEntryListWidget: ArchiveEntryListWidget
    lateinit var addItemsToArchiveButton: Button
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

        addItemsToArchiveButton.active = false
        deleteButton.active = false
    }

    override fun render(poseStack: PoseStack, i: Int, j: Int, f: Float) {
        renderDirtBackground(1)
        super.render(poseStack, i, j, f)
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
        addItemsToArchiveButton = Button(
            ((this.width / 4) + 10) + (archiveEntryListWidget.rowWidth / 2) - this.width / 8,
            (this.height / 24) * 23,
            this.width / 4,
            20,
            literalText("Add items")
        ) {
            minecraft?.setScreen(null)
            minecraft?.setScreen(AddItemsScreen(this, currentArchive, this))
        }
        archiveListWidget =
            ArchiveListWidget(this.width / 4, 5, (this.height / 24) * 21 - 10, 10, ((this.height / 24) * 21), 24, this)
        deleteButton = Button(
            5,
            (this.height / 24) * 22,
            (this.width / 8) - 2,
            20,
            literalText("Delete") { color = 0x99620401.toInt() }) {
            if (!it.active) return@Button
            if (currentArchive == "") return@Button
            settings.items.remove(currentArchive)
            itemIds.remove(currentArchive)
            saveConfig()
            currentArchive = ""
            it.active = false
            archiveEntryListWidget.refreshEntries()
            archiveListWidget.refreshEntries()
            addItemsToArchiveButton.active = false
        }
        createArchiveButton = Button(
            5 + ((this.width / 8) - 2) + 2, (this.height / 24) * 22, (this.width / 8), 20, literalText("Create archive")
        ) {
            minecraft?.setScreen(null)
            minecraft?.setScreen(CreateArchiveScreen(this, this))
        }
        doneButton = Button(5, (this.height / 24) * 23, this.width / 4, 20, literalText("Done")) {
            saveConfig()
            minecraft?.setScreen(previousScreen)
        }
    }
}