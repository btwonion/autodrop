@file:OptIn(ExperimentalTime::class)

package dev.nyon.autodrop.config.screen.root

import dev.nyon.autodrop.config.Archive
import dev.nyon.autodrop.config.config
import dev.nyon.autodrop.extensions.screenHeight
import dev.nyon.autodrop.extensions.screenWidth
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ObjectSelectionList
//? if >1.21.8
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import kotlin.math.max
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import dev.nyon.autodrop.AutoDrop.minecraft as internalMinecraft

class ArchivesWidget(private val archiveScreen: ArchiveScreen) : ObjectSelectionList<ArchivesWidgetEntry>(
    internalMinecraft,
    screenWidth / 4 - OUTER_PAD,
    screenHeight - 3 * OUTER_PAD - 12 - 5 * 20,
    OUTER_PAD,
    internalMinecraft.font.lineHeight + 2 * INNER_PAD
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

    /*? if <1.21.4 {*/
    /*override fun getScrollbarPosition(): Int {
        return right - 7
    }

    override fun getMaxScroll(): Int {
        return max(0, maxPosition - getHeight() + INNER_PAD)
    }*//*?}*/

    override fun renderWidget(guiGraphics: GuiGraphics, i: Int, j: Int, f: Float) {
        width = screenWidth / 4 - OUTER_PAD
        height = screenHeight - 3 * OUTER_PAD - 12 - 5 * 20
        super.renderWidget(guiGraphics, i, j, f)
    }

    fun refreshEntries() {
        /*? if <1.21.4 {*/ /*scrollAmount = 0.0 *//*?} else {*/ setScrollAmount(0.0) /*?}*/
        clearEntries()
        config.archives.map { ArchivesWidgetEntry(it, archiveScreen) }.forEach(::addEntry)
    }
}

class ArchivesWidgetEntry(private val archive: Archive, private val archiveScreen: ArchiveScreen) :
    ObjectSelectionList.Entry<ArchivesWidgetEntry>() {

    //? if >1.21.8 {
    override fun renderContent(graphics: GuiGraphics, mouseX: Int, mouseY: Int, hovered: Boolean, delta: Float) {
        renderAdItems(graphics)
    }
    //?} else {
    /*override fun render(
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
        renderAdItems(guiGraphics, x, y, width, height)
    }
    *///?}

    private fun renderAdItems(guiGraphics: GuiGraphics/*? if <=1.21.8 {*//*, x: Int, y: Int, width: Int, height: Int*//*?}*/) {
        if ((archiveScreen.selected ?: return).name == archive.name) guiGraphics.fill(
            x - 3, y - 2, x + width, y + height, 0xFF404040.toInt()
        )

        // Draw archive name
        val hundredPercentAlphaWhite = 0xFFFFFFFF.toInt()
        guiGraphics.drawString(
            internalMinecraft.font,
            Component.literal(archive.name),
            x,
            y + height / 2 - internalMinecraft.font.lineHeight / 2,
            0xFFFFFFFF.toInt()
        )

        // tick box - outer rectangle
        val rightX = x + width - INNER_PAD - 2
        val size = height - 2
        guiGraphics.hLine(rightX, rightX - size, y, hundredPercentAlphaWhite)
        guiGraphics.hLine(rightX, rightX - size, y + size - 1, hundredPercentAlphaWhite)
        guiGraphics.vLine(rightX, y, y + size - 1, hundredPercentAlphaWhite)
        guiGraphics.vLine(rightX - size, y, y + size - 1, hundredPercentAlphaWhite)

        // tick box - inner square
        if (archive.enabled) guiGraphics.fill(
            rightX - 1, y + 2, rightX - size + 2, y + size - 2, hundredPercentAlphaWhite
        )
    }

    private var lastClick: Instant? = null
    override fun mouseClicked(/*? if >1.21.8 {*/ mouseButtonEvent: MouseButtonEvent, bl: Boolean /*?} else {*/ /*mouseX: Double, mouseY: Double, button: Int *//*?}*/): Boolean {
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