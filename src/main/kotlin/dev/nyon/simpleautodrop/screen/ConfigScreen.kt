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

    val archiveEntryListWidget = ArchiveEntryListWidget(
        700, 230, 470, 10, 480, 24, currentArchive
    )
    val addItemToArchiveButton = Button(500, 482, 200, 20, literalText("Add items")) {
        minecraft?.setScreen(null)
        minecraft?.setScreen(AddItemsScreen(this, currentArchive, this))
    }

    val archiveListWidget = ArchiveListWidget(205, 5, 440, 10, 450, 24, this)
    val deleteButton = Button(5, 458, 100, 20, literalText("Delete")) {
        if (!it.active) return@Button
        if (currentArchive == "") return@Button
        settings.items.remove(currentArchive)
        itemIds.remove(currentArchive)
        saveConfig()
        currentArchive = ""
        it.active = false
        archiveEntryListWidget.refreshEntries()
        archiveListWidget.refreshEntries()
        addItemToArchiveButton.active = false
    }
    private val createArchiveButton = Button(108, 458, 100, 20, literalText("Create archive")) {
        minecraft?.setScreen(null)
        minecraft?.setScreen(CreateArchiveScreen(this, this))
    }

    private val doneButton = Button(5, 480, 205, 20, literalText("Done")) {
        saveConfig()
        minecraft?.setScreen(previousScreen)
    }

    override fun init() {
        addRenderableWidget(archiveEntryListWidget)
        addRenderableWidget(archiveListWidget)
        addRenderableWidget(deleteButton)
        addRenderableWidget(doneButton)
        addRenderableWidget(createArchiveButton)
        addRenderableWidget(addItemToArchiveButton)

        addItemToArchiveButton.active = false
        deleteButton.active = false
    }

    override fun render(poseStack: PoseStack, i: Int, j: Int, f: Float) {
        renderDirtBackground(1)
        super.render(poseStack, i, j, f)
    }

    override fun onClose() {
        minecraft?.setScreen(previousScreen)
    }
}