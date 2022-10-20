package dev.nyon.simpleautodrop.screen.archive

import com.mojang.blaze3d.vertex.PoseStack
import dev.nyon.simpleautodrop.config.reloadCachedIds
import dev.nyon.simpleautodrop.config.settings
import dev.nyon.simpleautodrop.screen.ConfigScreen
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiComponent
import net.minecraft.client.gui.components.ObjectSelectionList
import net.silkmc.silk.core.text.literalText

class ArchiveListWidget(
    private val _width: Int,
    private val _left: Int,
    private val _height: Int,
    private val _top: Int,
    bottom: Int,
    itemHeight: Int,
    private val configScreen: ConfigScreen
) : ObjectSelectionList<ArchiveEntry>(
    Minecraft.getInstance(), _width, _height, _top, bottom, itemHeight
) {

    init {
        refreshEntries()
        setLeftPos(_left)
    }

    override fun render(matrices: PoseStack, mouseX: Int, mouseY: Int, delta: Float) {
        if (settings.items.isEmpty()) {
            GuiComponent.drawCenteredString(
                matrices,
                minecraft.font,
                literalText("No existing archives"),
                _left + (_width / 2),
                _top + (_height / 2),
                0x80FFFFFF.toInt()
            )
            return
        }
        super.render(matrices, mouseX, mouseY, delta)
    }

    override fun getScrollbarPosition(): Int = rowLeft + rowWidth

    override fun getRowWidth(): Int = _width - 20

    fun refreshEntries() {
        clearEntries()

        settings.items.forEach {
            addEntry(ArchiveEntry(it.key, configScreen))
        }
    }

    var lastClick: Pair<String, Long> = "" to System.currentTimeMillis()
    fun handleMouseClick(archive: String) {
        if (lastClick.first == archive && System.currentTimeMillis() - lastClick.second < 250) {
            if (!settings.currentArchives.contains(archive)) settings.currentArchives += archive
            else settings.currentArchives -= archive
            reloadCachedIds()
        }
        lastClick = archive to System.currentTimeMillis()
    }

}