package dev.nyon.autodrop.config.screen

import dev.nyon.autodrop.config.Archive
import dev.nyon.autodrop.config.ItemIdentificator
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
import dev.nyon.autodrop.minecraft as internalMinecraft

class ArchiveItemsWidget(var archive: Archive) : ObjectSelectionList<ArchiveItemEntry>(
    internalMinecraft, 0, 0, OUTER_PAD, 20 + 2 * INNER_PAD
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

    override fun getScrollbarPosition(): Int {
        return right - 7
    }

    override fun getMaxScroll(): Int {
        return max(0, maxPosition - getHeight() + INNER_PAD)
    }

    override fun renderWidget(guiGraphics: GuiGraphics, i: Int, j: Int, f: Float) {
        width = (internalMinecraft.screen!!.width / 4) * 3 - 2 * OUTER_PAD
        height = internalMinecraft.screen!!.height - 2 * OUTER_PAD
        super.renderWidget(guiGraphics, i, j, f)
    }

    fun refreshEntries() {
        scrollAmount = 0.0
        clearEntries()
        archive.entries.map {
            ArchiveItemEntry(it) {
                archive.entries.remove(it)
                refreshEntries()
            }
        }.forEach(::addEntry)
    }
}

class ArchiveItemEntry(private val itemIdentificatior: ItemIdentificator, private val onRemove: () -> Unit) :
    ObjectSelectionList.Entry<ArchiveItemEntry>() {
    private val item: Item = itemIdentificatior.type ?: Items.AIR
    private val itemLocationString = BuiltInRegistries.ITEM.getKey(item).run {
        val string = toString()
        if (string.length > 20) return@run "${string.take(17)}..."
        else return@run string
    }

    private val removeButton = Button.builder(screenComponent("widget.items.remove")) {
        onRemove()
    }.width(75).build()

    private val modifyButton = Button.builder(screenComponent("widget.items.modify")) {
        // TODO: open modify screen
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
        val textPad = 7
        guiGraphics.renderItem(ItemStack(item), x + INNER_PAD, y + 4)
        guiGraphics.drawString(
            internalMinecraft.font, itemLocationString, x + INNER_PAD * 2 + height, y + textPad, 0xFFFFFF
        )

        val twentyCharacterWidth = internalMinecraft.font.width(Component.literal("minecraft:chestplate")) * 2
        guiGraphics.drawCenteredString(
            internalMinecraft.font,
            screenComponent("widget.items.component.${!itemIdentificatior.components.isEmpty}"),
            x + INNER_PAD * 3 + twentyCharacterWidth,
            y + textPad,
            0xFFFFFF
        )
        guiGraphics.drawString(
            internalMinecraft.font,
            screenComponent("widget.items.amount", itemIdentificatior.amount.toString()),
            x + INNER_PAD * 4 + (twentyCharacterWidth * 1.5).toInt(),
            y + textPad,
            0xFFFFFF
        )

        removeButton.setPosition(x + width - removeButton.width, y)
        removeButton.render(guiGraphics, mouseX, mouseY, delta)
        modifyButton.setPosition(x + width - removeButton.width - (INNER_PAD * 0.5).toInt() - modifyButton.width, y)
        modifyButton.render(guiGraphics, mouseX, mouseY, delta)
    }

    override fun mouseClicked(d: Double, e: Double, i: Int): Boolean {
        if (removeButton.isMouseOver(d, e)) return removeButton.mouseClicked(d, e, i)
        if (modifyButton.isMouseOver(d, e)) return modifyButton.mouseClicked(d, e, i)
        return super.mouseClicked(d, e, i)
    }

    override fun getNarration(): Component {
        return Component.literal(item.description.toString())
    }
}