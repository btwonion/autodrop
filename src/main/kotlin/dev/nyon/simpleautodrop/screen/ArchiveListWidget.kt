package dev.nyon.simpleautodrop.screen

import dev.nyon.simpleautodrop.config.settings
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.ContainerObjectSelectionList
import net.minecraft.client.gui.narration.NarrationElementOutput

class ArchiveListWidget(
    client: Minecraft,
    private val _width: Int,
    left: Int,
    height: Int,
    top: Int,
    bottom: Int,
    itemHeight: Int,
    val archive: String
) : ContainerObjectSelectionList<ArchiveEntryWidget>(client, _width, height, top, bottom, itemHeight) {

    init {
        refreshEntries()
        setLeftPos(left)
    }

    override fun updateNarration(builder: NarrationElementOutput) {}

    override fun getRowWidth(): Int {
        return _width - 20
    }

    override fun getScrollbarPosition(): Int = rowLeft + rowWidth

    fun refreshEntries() {
        clearEntries()

        settings.items[archive]?.sortedBy { it.description.string }?.forEach {
            addEntry(ArchiveEntryWidget(it, this@ArchiveListWidget))
        }
    }

}