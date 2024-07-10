package dev.nyon.autodrop.config.screen.root

import dev.nyon.autodrop.config.Archive
import dev.nyon.autodrop.config.config
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ObjectSelectionList
import net.minecraft.network.chat.Component
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds
import dev.nyon.autodrop.minecraft as internalMinecraft

class ArchivesWidget(private val archiveScreen: ArchiveScreen) : ObjectSelectionList<ArchivesWidgetEntry>(
    internalMinecraft, 0, 0, OUTER_PAD, internalMinecraft.font.lineHeight + 2 * INNER_PAD
) {
    override fun getX(): Int {
        return OUTER_PAD
    }

    override fun getRowLeft(): Int {
        return x + INNER_PAD
    }

    override fun getRowWidth(): Int {
        return width - 2 * INNER_PAD
    }

    override fun getScrollbarPosition(): Int {
        return right - 7
    }

    override fun getMaxScroll(): Int {
        return max(0, maxPosition - getHeight() + INNER_PAD)
    }

    override fun renderWidget(guiGraphics: GuiGraphics, i: Int, j: Int, f: Float) {
        width = (internalMinecraft.screen!!.width / 4) - 2 * OUTER_PAD
        height = internalMinecraft.screen!!.height - 3 * OUTER_PAD - 12 - 5 * 20
        super.renderWidget(guiGraphics, i, j, f)
    }

    fun refreshEntries() {
        scrollAmount = 0.0
        clearEntries()
        config.archives.map { ArchivesWidgetEntry(it, archiveScreen) }.forEach(::addEntry)
    }
}

class ArchivesWidgetEntry(private val archive: Archive, private val archiveScreen: ArchiveScreen) :
    ObjectSelectionList.Entry<ArchivesWidgetEntry>() {
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
        if (archiveScreen.selected.name == archive.name) guiGraphics.fill(
            x - 3, y - 2, x + width - 2, y + height + 2, 0xFF404040.toInt()
        )

        // Draw archive name
        val hundredPercentAlphaWhite = 0xFFFFFFFF.toInt()
        guiGraphics.drawString(internalMinecraft.font, Component.literal(archive.name), x, y + INNER_PAD / 2, 0xFFFFFF)

        // tick box - outer rectangle
        val rightX = x + width - INNER_PAD
        guiGraphics.hLine(rightX, rightX - height, y, hundredPercentAlphaWhite)
        guiGraphics.hLine(rightX, rightX - height, y + height - 1, hundredPercentAlphaWhite)
        guiGraphics.vLine(rightX, y, y + height - 1, hundredPercentAlphaWhite)
        guiGraphics.vLine(rightX - height, y, y + height - 1, hundredPercentAlphaWhite)

        // tick box - inner square
        if (archive.enabled) guiGraphics.fill(
            rightX - 1, y + 2, rightX - height + 2, y + height - 2, hundredPercentAlphaWhite
        )
    }

    private var lastClick: Instant? = null
    override fun mouseClicked(d: Double, e: Double, i: Int): Boolean {
        archiveScreen.select(archive)
        val now = Clock.System.now()
        if (lastClick != null && now - lastClick!! < 200.milliseconds) archive.enabled = !archive.enabled
        lastClick = now
        return false
    }

    override fun getNarration(): Component {
        return Component.literal(archive.name)
    }
}