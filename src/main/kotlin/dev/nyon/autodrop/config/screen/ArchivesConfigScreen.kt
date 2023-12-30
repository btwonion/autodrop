package dev.nyon.autodrop.config.screen

import dev.nyon.autodrop.config.settings
import dev.nyon.autodrop.minecraft
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

/**
 * @author btwonion
 * @since 19/12/2023
 */
class ArchivesConfigScreen(private val parent: Screen? = null) :
    Screen(Text.translatable("menu.autodrop.archives.overview.title")) {

    /**
     * should be 1/9 of the screen
     */
    private val archiveListLeft
        get() = width / 9

    /**
     * should be 1/3 of the screen minus the two paddings on each side
     */
    private val archiveListWidth
        get() = (width / 3) - ((width / 9) * 2)

    /**
     * should be the other 2/3 of the screen plus the left padding
     */
    private val itemListLeft
        get() = width / 3 + width / 9

    /**
     * should be the other 2/3 of the screen minus the two paddings on each side
     */
    private val itemListWidth
        get() = (2 * (width / 3)) - ((width / 9) * 2)

    /**
     * should be 1/9 of the screen height
     */
    private val contentTop
        get() = height / 9

    private val titleTop
        get() = contentTop / 2

    private val contentHeight
        get() = height - 2 * contentTop

    private var currentArchive: String? = settings.archives.firstOrNull()?.name
    private lateinit var archiveListWidget: ArchiveList
    private val itemListWidget: ItemList?
        get() {
            if (currentArchive == null) return null
            return ItemList(
                itemListWidth, contentHeight, contentTop, height / 14, itemListLeft, currentArchive ?: return null
            ) { archiveListWidget.loadEntries() }.also { it.loadEntries() }
        }

    override fun init() {
        super.init()

        archiveListWidget = ArchiveList(
            archiveListWidth,
            contentHeight,
            contentTop,
            height / 14,
            archiveListLeft,
            { minecraft.setScreen(parent) }) { currentArchive = it }.also { it.loadEntries() }

        addDrawableChild(archiveListWidget)
        itemListWidget?.let { addDrawableChild(it) }
    }

    override fun render(gui: DrawContext, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(gui, mouseX, mouseY, partialTick)
        renderBackgroundTexture(gui)

        gui.drawCenteredTextWithShadow(
            minecraft.textRenderer ?: return,
            Text.translatable("menu.autodrop.archives.overview.title"),
            width / 2,
            titleTop,
            0xFFFFFF
        )
    }
}