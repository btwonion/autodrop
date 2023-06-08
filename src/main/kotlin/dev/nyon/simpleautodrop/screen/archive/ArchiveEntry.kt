package dev.nyon.simpleautodrop.screen.archive

import dev.nyon.simpleautodrop.config.settings
import dev.nyon.simpleautodrop.minecraft
import dev.nyon.simpleautodrop.screen.ConfigScreen
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ObjectSelectionList
import net.minecraft.network.chat.Component
import kotlin.streams.toList

class ArchiveEntry(private val archive: String, private val configScreen: ConfigScreen) :
    ObjectSelectionList.Entry<ArchiveEntry>() {
    override fun render(
        matrices: GuiGraphics,
        index: Int,
        y: Int,
        x: Int,
        entryWidth: Int,
        entryHeight: Int,
        mouseX: Int,
        mouseY: Int,
        hovered: Boolean,
        tickDelta: Float
    ) {
        if (hovered) matrices.fill(
            x - 1, y + entryHeight + 1, x + entryWidth - 5, y - 1, 0x90000000.toInt()
        )

        if (settings.activeArchives.contains(archive)) matrices.drawString(
            minecraft.font,
            Component.literal("enabled"),
            (x + entryWidth) - ("enabled".chars().toList().size * 10),
            (y + 6.5).toInt(),
            0x991D5941.toInt(),
            false
        )

        matrices.drawString(
            minecraft.font,
            Component.literal(archive),
            x + 5,
            (y + 6.5).toInt(),
            0x99FFFFFF.toInt(),
            false
        )
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        configScreen.deleteButton.active = true
        configScreen.currentArchive = archive
        configScreen.archiveEntryListWidget.archive = archive
        configScreen.archiveEntryListWidget.refreshEntries()
        configScreen.addItemsToArchiveButton.active = true
        configScreen.setLockedSlotsButton.active = true
        configScreen.archiveListWidget.handleMouseClick(archive)
        return true
    }

    override fun getNarration(): Component = Component.literal(archive)
}