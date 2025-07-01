package dev.nyon.autodrop.config.screen.root

import dev.nyon.autodrop.config.Archive
import dev.nyon.autodrop.config.ArchiveEntry
import dev.nyon.autodrop.config.config
import dev.nyon.autodrop.config.reloadArchiveProperties
import dev.nyon.autodrop.config.screen.create.CreateArchiveScreen
import dev.nyon.autodrop.config.screen.ignored.IgnoredSlotsScreen
import dev.nyon.autodrop.config.screen.modify.ModifyEntryScreen
import dev.nyon.autodrop.extensions.screenComponent
import dev.nyon.konfig.config.saveConfig
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import dev.nyon.autodrop.AutoDrop.minecraft as internalMinecraft

const val INNER_PAD = 5
const val OUTER_PAD = 10

class ArchiveScreen(private val parent: Screen?) : Screen(screenComponent("title")) {
    var selected: Archive? = config.archives.firstOrNull()

    private val archivesWidget = ArchivesWidget(this).also {
        it.refreshEntries()
    }

    val archiveItemsWidget = ArchiveItemsWidget(selected, this@ArchiveScreen).also {
        it.refreshEntries()
    }

    private val doneButton = Button.builder(screenComponent("done")) {
        onClose()
    }.build()

    private val setIgnoredSlotsButton = Button.builder(screenComponent("ignored")) {
        internalMinecraft.setScreen(IgnoredSlotsScreen(selected ?: return@builder, this@ArchiveScreen))
    }.build().also { it.active = selected != null}

    private val createArchiveButton = Button.builder(screenComponent("create")) {
        internalMinecraft.setScreen(CreateArchiveScreen(this@ArchiveScreen) {
            archivesWidget.refreshEntries()
            select(it)
            setIgnoredSlotsButton.active = true
            addIdentifierButton.active = true
            deleteArchiveButton.active = true
        })
    }.build()

    private val deleteArchiveButton = Button.builder(screenComponent("delete").withStyle(ChatFormatting.DARK_RED)) {
        config.archives.remove(selected)
        selected = config.archives.firstOrNull()

        if (selected == null) {
            setIgnoredSlotsButton.active = false
            addIdentifierButton.active = false
            it.active = false
        }

        archivesWidget.refreshEntries()
        archiveItemsWidget.archive = selected
        archiveItemsWidget.refreshEntries()
    }.build().also { it.active = selected != null}

    private val addIdentifierButton = Button.builder(screenComponent("identifier")) {
        val newIdentifier = ArchiveEntry(null, "[]", 1, true)

        selected?.entries?.add(newIdentifier) ?: return@builder
        internalMinecraft.setScreen(
            ModifyEntryScreen(
                this@ArchiveScreen, newIdentifier
            )
        )
    }.build()

    override fun init() {
        addRenderableWidget(archivesWidget)
        addRenderableWidget(archiveItemsWidget)
        addRenderableWidget(doneButton)
        addRenderableWidget(setIgnoredSlotsButton)
        addRenderableWidget(createArchiveButton)
        addRenderableWidget(deleteArchiveButton)
        addRenderableWidget(addIdentifierButton)
        super.init()
    }

    override fun onClose() {
        internalMinecraft.setScreen(parent)
        saveConfig(config)
        reloadArchiveProperties()
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, tickDelta: Float) {
        doneButton.setPosition(OUTER_PAD, internalMinecraft.screen!!.height - OUTER_PAD - 20)
        doneButton.width = internalMinecraft.screen!!.width / 4 - OUTER_PAD

        setIgnoredSlotsButton.setPosition(OUTER_PAD, internalMinecraft.screen!!.height - OUTER_PAD - 2 * 20 - 3)
        setIgnoredSlotsButton.width = internalMinecraft.screen!!.width / 4 - OUTER_PAD

        createArchiveButton.setPosition(OUTER_PAD, internalMinecraft.screen!!.height - OUTER_PAD - 3 * 20 - 6)
        createArchiveButton.width = internalMinecraft.screen!!.width / 4 - OUTER_PAD

        deleteArchiveButton.setPosition(OUTER_PAD, internalMinecraft.screen!!.height - OUTER_PAD - 4 * 20 - 9)
        deleteArchiveButton.width = internalMinecraft.screen!!.width / 4 - OUTER_PAD

        addIdentifierButton.setPosition(OUTER_PAD, internalMinecraft.screen!!.height - OUTER_PAD - 5 * 20 - 12)
        addIdentifierButton.width = internalMinecraft.screen!!.width / 4 - OUTER_PAD

        super.render(guiGraphics, mouseX, mouseY, tickDelta)
    }

    fun select(archive: Archive) {
        selected = archive
        archiveItemsWidget.archive = archive
        archiveItemsWidget.refreshEntries()
    }
}