package dev.nyon.autodrop.config.screen

import dev.nyon.autodrop.config.Archive
import dev.nyon.autodrop.extensions.screenComponent
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen

const val INNER_PAD = 5
const val OUTER_PAD = 10

class ArchiveScreen(private val parent: Screen?) : Screen(screenComponent("title")) {
    var selected: Archive? = null

    @Suppress("unused")
    private val archivesWidget = ArchivesWidget(this).also {
        addWidget(it)
        it.refreshEntries()
    }

    override fun onClose() {
        minecraft!!.setScreen(parent)
    }

    override fun render(guiGraphics: GuiGraphics, i: Int, j: Int, f: Float) {
        renderBackground(guiGraphics, i, j, f)
        archivesWidget.render(guiGraphics, i, j, f)
    }

    fun select(archive: Archive) {
        selected = archive
    }
}