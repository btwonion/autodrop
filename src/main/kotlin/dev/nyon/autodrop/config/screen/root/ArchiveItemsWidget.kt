package dev.nyon.autodrop.config.screen.root

import dev.nyon.autodrop.config.Archive
import dev.nyon.autodrop.config.ArchiveEntry
import dev.nyon.autodrop.config.screen.modify.ModifyEntryScreen
import dev.nyon.autodrop.extensions.narration
import dev.nyon.autodrop.extensions.openScreen
import dev.nyon.autodrop.extensions.screenComponent
import dev.nyon.autodrop.extensions.screenHeight
import dev.nyon.autodrop.extensions.screenWidth
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.ObjectSelectionList
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import dev.nyon.autodrop.AutoDrop.minecraft as internalMinecraft

class ArchiveItemsWidget(var archive: Archive?, private val parent: ArchiveScreen) :
    ObjectSelectionList<ArchiveItemEntry>(
        internalMinecraft,
        (screenWidth / 4) * 3 - 2 * OUTER_PAD,
        screenHeight - 2 * OUTER_PAD,
        OUTER_PAD,
        internalMinecraft.font.lineHeight * 3 + 4 * INNER_PAD
    ) {
    override fun getX(): Int {
        return screenWidth / 4 + OUTER_PAD
    }

    override fun getRowLeft(): Int {
        return x + INNER_PAD
    }

    override fun getRowWidth(): Int {
        return width - 2 * INNER_PAD
    }

    override fun extractWidgetRenderState(guiGraphics: GuiGraphicsExtractor, i: Int, j: Int, f: Float) {
        width = (screenWidth / 4) * 3 - 2 * OUTER_PAD
        height = screenHeight - 2 * OUTER_PAD
        super.extractWidgetRenderState(guiGraphics, i, j, f)
    }

    fun refreshEntries() {
        setScrollAmount(0.0)
        clearEntries()
        if (archive == null) return
        archive!!.entries.map {
            ArchiveItemEntry(it, parent) {
                archive!!.entries.remove(it)
                refreshEntries()
            }
        }.forEach(::addEntry)
    }
}

class ArchiveItemEntry(
    private val archiveEntry: ArchiveEntry, private val parent: ArchiveScreen, private val onRemove: () -> Unit
) : ObjectSelectionList.Entry<ArchiveItemEntry>() {
    private val item: Item = archiveEntry.type ?: Items.AIR
    private val itemLocationString = BuiltInRegistries.ITEM.getKey(item).run {
        val string = toString()
        if (string.length > 25) return@run "${string.take(22)}..."
        else return@run string
    }

    private val removeButton = Button.builder(screenComponent("widget.items.remove")) {
        onRemove()
    }.width(75).build()

    private val modifyButton = Button.builder(screenComponent("widget.items.modify")) {
        internalMinecraft.openScreen(ModifyEntryScreen(parent, archiveEntry))
    }.width(75).build()

    override fun extractContent(
        guiGraphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        hovered: Boolean,
        delta: Float
    ) {
        val textX = x + internalMinecraft.font.lineHeight * 3 + INNER_PAD
        guiGraphics.item(item.defaultInstance, x + INNER_PAD, y + INNER_PAD + internalMinecraft.font.lineHeight)
        guiGraphics.text(
            internalMinecraft.font, itemLocationString, textX, y + INNER_PAD, 0xFFFFFFFF.toInt()
        )

        guiGraphics.text(
            internalMinecraft.font,
            screenComponent("widget.items.component.${archiveEntry.predicate.length > 2}"),
            textX,
            y + internalMinecraft.font.lineHeight + INNER_PAD * 2,
            0xFFFFFFFF.toInt()
        )
        guiGraphics.text(
            internalMinecraft.font,
            screenComponent("widget.items.amount", archiveEntry.amount.toString()),
            textX,
            y + internalMinecraft.font.lineHeight * 2 + INNER_PAD * 3,
            0xFFFFFFFF.toInt()
        )

        removeButton.setPosition(x + width - removeButton.width - INNER_PAD, y + height / 2 - 10)
        removeButton.extractRenderState(guiGraphics, mouseX, mouseY, delta)
        modifyButton.setPosition(
            x + width - removeButton.width - INNER_PAD * 2 - modifyButton.width, y + height / 2 - 10
        )
        modifyButton.extractRenderState(guiGraphics, mouseX, mouseY, delta)
    }

    override fun mouseClicked(mouseButtonEvent: MouseButtonEvent, bl: Boolean): Boolean {
        if (removeButton.isMouseOver(mouseButtonEvent.x, mouseButtonEvent.y)) return removeButton.mouseClicked(
            mouseButtonEvent,
            bl
        )
        if (modifyButton.isMouseOver(mouseButtonEvent.x, mouseButtonEvent.y)) return modifyButton.mouseClicked(
            mouseButtonEvent,
            bl
        )
        return super.mouseClicked(mouseButtonEvent, bl)
    }

    override fun getNarration(): Component {
        return item.narration
    }
}