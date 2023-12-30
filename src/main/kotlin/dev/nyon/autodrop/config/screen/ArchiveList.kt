package dev.nyon.autodrop.config.screen

import dev.nyon.autodrop.config.models.Archive
import dev.nyon.autodrop.config.settings
import dev.nyon.autodrop.minecraft
import dev.nyon.autodrop.util.button
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget
import net.minecraft.text.Text

/**
 * @author btwonion
 * @since 19/12/2023
 */
class ArchiveList(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val left: Int,
    closeCallback: () -> Unit,
    private val archiveSelectCallback: (String) -> Unit
) : AlwaysSelectedEntryListWidget<ArchiveListEntry>(
    minecraft, x, y, width, height
) {
    private val archiveListEntryPlaceholder = ArchiveListEntryPlaceholder(closeCallback) { loadEntries() }

    override fun getRowLeft(): Int {
        return left
    }

    fun loadEntries() {
        clearEntries()

        addEntryToTop(archiveListEntryPlaceholder)

        settings.archives.forEach { archive ->
            addEntry(ArchiveListEntryArchive(archive.name, archiveSelectCallback))
        }
    }
}

abstract class ArchiveListEntry : AlwaysSelectedEntryListWidget.Entry<ArchiveListEntry>()

class ArchiveListEntryArchive(private val archiveName: String, val archiveSelectCallback: (String) -> Unit) :
    ArchiveListEntry() {
    // Pair of minX minY and maxX maxY
    private var lastTickBoxPos = Pair(0 to 0, 0 to 0)

    override fun render(
        gui: DrawContext,
        index: Int,
        top: Int,
        left: Int,
        width: Int,
        height: Int,
        mouseX: Int,
        mouseY: Int,
        hovering: Boolean,
        partialTick: Float
    ) {
        val padding = width / 9
        val topBottomPadding = height / 4
        // Archive name
        gui.drawText(
            minecraft.textRenderer, Text.literal(archiveName), left + padding, top + height / 2, 0xFFFFFF, false
        )
        // TickBox
        val tickBoxSize = (width - 2 * padding) / 5
        gui.drawTickBox(
            settings.activeArchives.contains(archiveName), top + topBottomPadding, left + tickBoxSize, tickBoxSize
        )
    }

    override fun getNarration(): Text {
        return Text.translatable("menu.autodrop.archives.overview.title")
    }

    private fun DrawContext.drawTickBox(ticked: Boolean, top: Int, left: Int, size: Int) {
        lastTickBoxPos = (left to top) to (left + size to top + size)

        // draw box
        listOf(left, left + size).forEach { x -> drawVerticalLine(x, top, top + size, 0xFFFFFF) }
        listOf(top, top + size).forEach { y -> drawHorizontalLine(left, left + size, y, 0xFFFFFF) }

        if (!ticked) return
        // draw checkmark
        (1..size).forEach {
            fill(left + it, top + it, left + it, top + it, 0xFFFFFF)
            fill(left + size - it, top + size - it, left + size - it, top + size - it, 0xFFFFFF)
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        archiveSelectCallback(archiveName)
        if (!(mouseX >= lastTickBoxPos.first.first && mouseX <= lastTickBoxPos.second.first)) return false
        if (!(mouseY >= lastTickBoxPos.first.second && mouseY <= lastTickBoxPos.second.second)) return false
        settings.activeArchives.add(archiveName)
        return true
    }
}

class ArchiveListEntryPlaceholder(val closeCallback: () -> Unit, val archiveReloadCallback: () -> Unit) :
    ArchiveListEntry() {
    override fun render(
        guiGraphics: DrawContext,
        index: Int,
        top: Int,
        left: Int,
        width: Int,
        height: Int,
        mouseX: Int,
        mouseY: Int,
        hovering: Boolean,
        partialTick: Float
    ) {
        val padding = width / 10
        val topBottomPadding = height / 10
        val buttonWidth = width / 2 - 2 * padding
        val buttonHeight = height - 2 * topBottomPadding

        // Create archive button
        button(
            left + padding,
            top + topBottomPadding,
            buttonWidth,
            buttonHeight,
            Text.translatable("menu.autodrop.archives.createarchive.button")
        ) {
            settings.archives.add(Archive("Archive ${settings.archives.size + 1}", mutableListOf(), mutableListOf()))
            archiveReloadCallback()
        }.render(guiGraphics, mouseX, mouseY, partialTick)

        // Done button
        button(
            left + width / 2 + padding,
            top + topBottomPadding,
            buttonWidth,
            buttonHeight,
            Text.translatable("menu.autodrop.archives.done")
        ) { closeCallback() }.render(guiGraphics, mouseX, mouseY, partialTick)
    }

    override fun getNarration(): Text {
        return Text.translatable("menu.autodrop.archives.overview.title")
    }
}