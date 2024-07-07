package dev.nyon.autodrop.config.screen

import dev.nyon.autodrop.config.Archive
import dev.nyon.autodrop.config.config
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ObjectSelectionList
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import dev.nyon.autodrop.minecraft as internalMinecraft

object ArchivesWidget : ObjectSelectionList<ArchivesWidgetEntry>(
    internalMinecraft,
    0,
    0,
    OUTER_PAD,
    internalMinecraft.font.lineHeight + 2 * INNER_PAD
) {

    override fun getX(): Int {
        return OUTER_PAD
    }

    override fun getRowLeft(): Int {
        return x + INNER_PAD
    }

    override fun getWidth(): Int {
        return (internalMinecraft.screen!!.width / 4) - 2 * OUTER_PAD
    }

    override fun getHeight(): Int {
        return (internalMinecraft.screen!!.height / 4) * 3 - 2 * OUTER_PAD
    }

    override fun getRowWidth(): Int {
        return getWidth() - 2 * INNER_PAD
    }

    fun refreshEntries() {
        clearEntries()
        config.archives.forEach { archive ->
            addEntry(ArchivesWidgetEntry(archive))
        }
    }
}

class ArchivesWidgetEntry(private val archive: Archive) : ObjectSelectionList.Entry<ArchivesWidgetEntry>() {
    override fun render(
        guiGraphics: GuiGraphics,
        index: Int,
        y: Int,
        x: Int,
        width: Int,
        height: Int,
        mouseX: Int,
        mouseY: Int,
        isSelected: Boolean,
        delta: Float
    ) {
        // Draw archive name
        val hundredPercentAlphaWhite = 0xFFFFFFFF.toInt()
        val textPad = height - internalMinecraft.font.lineHeight / 2
        guiGraphics.drawString(internalMinecraft.font, Component.literal(archive.name), x, y + textPad / 2, 0xFFFFFF)

        // tick box - outer rectangle
        val rightX = x + width
        guiGraphics.hLine(rightX, rightX - height, y, hundredPercentAlphaWhite)
        guiGraphics.hLine(rightX, rightX - height, y + height - 1, hundredPercentAlphaWhite)
        guiGraphics.vLine(rightX, y, y + height - 1, hundredPercentAlphaWhite)
        guiGraphics.vLine(rightX - height, y, y + height - 1, hundredPercentAlphaWhite)

        // tick box - inner square
        if (archive.enabled) guiGraphics.fill(
            rightX - 1, y + 2, rightX - height + 2, y + height - 2, hundredPercentAlphaWhite
        )
    }

    override fun mouseClicked(d: Double, e: Double, i: Int): Boolean {
        archive.enabled = !archive.enabled
        return super.mouseClicked(d, e, i)
    }

    override fun getNarration(): Component {
        return Component.literal(archive.name)
    }
}