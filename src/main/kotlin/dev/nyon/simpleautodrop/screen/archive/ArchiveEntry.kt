package dev.nyon.simpleautodrop.screen.archive

import com.mojang.blaze3d.vertex.PoseStack
import dev.nyon.simpleautodrop.config.settings
import dev.nyon.simpleautodrop.screen.ConfigScreen
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiComponent
import net.minecraft.client.gui.components.ObjectSelectionList
import net.minecraft.network.chat.Component
import kotlin.streams.toList

class ArchiveEntry(private val archive: String, private val configScreen: ConfigScreen) :
    ObjectSelectionList.Entry<ArchiveEntry>() {
    override fun render(
        matrices: PoseStack,
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
        if (hovered) GuiComponent.fill(
            matrices, x - 1, y + entryHeight + 1, x + entryWidth - 5, y - 1, 0x90000000.toInt()
        )

        if (settings.activeArchives.contains(archive)) Minecraft.getInstance().font.draw(
            matrices,
            Component.literal("enabled"),
            (x + entryWidth) - ("enabled".chars().toList().size * 10).toFloat(),
            y + 6.5F,
            0x991D5941.toInt()
        )

        if (configScreen.currentArchive == archive) {
            matrices.line(x - 1, y - 1, x + entryWidth - 5, y)
            matrices.line(x - 1, y + entryHeight + 1, x + entryWidth - 5, y + entryHeight)
            matrices.line(x - 1, y - 1, x, y + entryHeight + 1)
            matrices.line(x + entryWidth - 5, y - 1, x + entryWidth - 4, y + entryHeight + 1)
        }

        Minecraft.getInstance().font.draw(
            matrices, Component.literal(archive), x + 5.toFloat(), y + 6.5F, 0x99FFFFFF.toInt()
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

private fun PoseStack.line(x1: Int, y1: Int, x2: Int, y2: Int) =
    GuiComponent.fill(this, x1, y1, x2, y2, 0x99FFFFFF.toInt())