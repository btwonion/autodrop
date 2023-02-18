package dev.nyon.simpleautodrop.screen.archiveEntry

import com.mojang.blaze3d.vertex.PoseStack
import dev.nyon.simpleautodrop.config.settings
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiComponent
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

    override fun render(matrices: PoseStack, mouseX: Int, mouseY: Int, delta: Float) {
        if (archive == "") {
            GuiComponent.drawCenteredString(
                matrices,
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



        settings.archives.first { it.name == archive }.items.sortedBy { it.description.string }.forEach {
            addEntry(ArchiveEntryWidget(it, this@ArchiveEntryListWidget))
        }
    }
}