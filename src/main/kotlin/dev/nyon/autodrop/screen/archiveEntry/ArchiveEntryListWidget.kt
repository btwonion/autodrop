package dev.nyon.autodrop.screen.archiveEntry

import dev.nyon.autodrop.config.settings
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ContainerObjectSelectionList
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component

class ArchiveEntryListWidget(
    private val _width: Int,
    private val _left: Int,
    private val _height: Int,
    private val _top: Int,
    bottom: Int,
    itemHeight: Int,
    var archive: String
) : ContainerObjectSelectionList<ArchiveEntryWidget>(
    Minecraft.getInstance(), _width, _height, _top, bottom, itemHeight
) {

    init {
        refreshEntries()
        setLeftPos(_left)
    }

    override fun render(matrices: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (archive == "") {
            matrices.drawCenteredString(
                minecraft.font,
                Component.literal("No archive selected"),
                _left + (_width / 2),
                _top + (_height / 2),
                0x80FFFFFF.toInt()
            )
            return
        }
        super.render(matrices, mouseX, mouseY, delta)
    }

    override fun updateNarration(builder: NarrationElementOutput) {}

    override fun getRowWidth(): Int {
        return _width - 20
    }

    override fun getScrollbarPosition(): Int = rowLeft + rowWidth

    fun refreshEntries() {
        clearEntries()

        settings.archives.find { it.name == archive }?.items?.sortedBy { it.path }?.forEach {
            addEntry(ArchiveEntryWidget(it, this@ArchiveEntryListWidget))
        }
    }
}