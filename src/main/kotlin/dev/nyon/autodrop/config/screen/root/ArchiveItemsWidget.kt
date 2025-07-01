package dev.nyon.autodrop.config.screen.root

import dev.nyon.autodrop.config.Archive
import dev.nyon.autodrop.config.ArchiveEntry
import dev.nyon.autodrop.config.screen.modify.ModifyEntryScreen
import dev.nyon.autodrop.extensions.narration
import dev.nyon.autodrop.extensions.screenComponent
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.ObjectSelectionList
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import kotlin.math.max
import dev.nyon.autodrop.AutoDrop.minecraft as internalMinecraft

class ArchiveItemsWidget(var archive: Archive?, private val parent: ArchiveScreen) :
    ObjectSelectionList<ArchiveItemEntry>(
        internalMinecraft, 0, 0, OUTER_PAD, internalMinecraft.font.lineHeight * 3 + 4 * INNER_PAD
    ) {
    override fun getX(): Int {
        return internalMinecraft.screen!!.width / 4 + OUTER_PAD
    }

    override fun getRowLeft(): Int {
        return x + INNER_PAD
    }

    override fun getRowWidth(): Int {
        return getWidth() - 2 * INNER_PAD
    }

    /*? if <1.21.4 {*/
    /*override fun getScrollbarPosition(): Int {
        return right - 7
    }

    override fun getMaxScroll(): Int {
        return max(0, maxPosition - getHeight() + INNER_PAD)
    }*//*?}*/

    override fun renderWidget(guiGraphics: GuiGraphics, i: Int, j: Int, f: Float) {
        width = (internalMinecraft.screen!!.width / 4) * 3 - 2 * OUTER_PAD
        height = internalMinecraft.screen!!.height - 2 * OUTER_PAD
        super.renderWidget(guiGraphics, i, j, f)
    }

    fun refreshEntries() {
        /*? if <1.21.4 {*/ /*scrollAmount = 0.0 *//*?} else {*/ setScrollAmount(0.0) /*?}*/
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
        internalMinecraft.setScreen(ModifyEntryScreen(parent, archiveEntry))
    }.width(75).build()

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
        val textX = x + internalMinecraft.font.lineHeight * 3 + INNER_PAD
        guiGraphics.renderItem(ItemStack(item), x + INNER_PAD, y + INNER_PAD + internalMinecraft.font.lineHeight)
        guiGraphics.drawString(
            internalMinecraft.font, itemLocationString, textX, y + INNER_PAD, 0xFFFFFFFF.toInt()
        )

        guiGraphics.drawString(
            internalMinecraft.font,
            screenComponent("widget.items.component.${archiveEntry.predicate.length > 2}"),
            textX,
            y + internalMinecraft.font.lineHeight + INNER_PAD * 2,
            0xFFFFFFFF.toInt()
        )
        guiGraphics.drawString(
            internalMinecraft.font,
            screenComponent("widget.items.amount", archiveEntry.amount.toString()),
            textX,
            y + internalMinecraft.font.lineHeight * 2 + INNER_PAD * 3,
            0xFFFFFFFF.toInt()
        )

        removeButton.setPosition(x + width - removeButton.width - INNER_PAD, y + height / 2 - 10)
        removeButton.render(guiGraphics, mouseX, mouseY, delta)
        modifyButton.setPosition(
            x + width - removeButton.width - INNER_PAD * 2 - modifyButton.width,
            y + height / 2 - 10
        )
        modifyButton.render(guiGraphics, mouseX, mouseY, delta)
    }

    override fun mouseClicked(d: Double, e: Double, i: Int): Boolean {
        if (removeButton.isMouseOver(d, e)) return removeButton.mouseClicked(d, e, i)
        if (modifyButton.isMouseOver(d, e)) return modifyButton.mouseClicked(d, e, i)
        return super.mouseClicked(d, e, i)
    }

    override fun getNarration(): Component {
        return item.narration
    }
}