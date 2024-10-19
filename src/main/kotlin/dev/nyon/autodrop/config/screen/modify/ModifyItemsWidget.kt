package dev.nyon.autodrop.config.screen.modify

import dev.nyon.autodrop.config.screen.root.INNER_PAD
import dev.nyon.autodrop.config.screen.root.OUTER_PAD
import dev.nyon.autodrop.extensions.narration
import dev.nyon.autodrop.extensions.screenComponent
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.ObjectSelectionList
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import kotlin.math.max
import kotlin.math.min
import dev.nyon.autodrop.minecraft as internalMinecraft

class ModifyItemsWidget(var input: String, private val onSelect: Item.() -> Unit) :
    ObjectSelectionList<ModifyItemsEntry>(
        internalMinecraft, 0, 0, 0, internalMinecraft.font.lineHeight + 2 * INNER_PAD
    ) {
    override fun getX(): Int {
        return internalMinecraft.screen!!.width / 4
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
        width = internalMinecraft.screen!!.width / 2
        height = internalMinecraft.screen!!.height / 6
        y = OUTER_PAD + INNER_PAD * 2 + internalMinecraft.font.lineHeight + 20
        super.renderWidget(guiGraphics, i, j, f)
    }

    fun refreshEntries() {
        scrollAmount = 0.0
        clearEntries()
        BuiltInRegistries.ITEM.sortedByDescending { item ->
            val itemDescription = item.narration.string
            val resourceLocationString = BuiltInRegistries.ITEM.getKey(item).toString()
            val cleanedResourceLocation = resourceLocationString.dropWhile { it != ':' }
            if (itemDescription.startsWith(input) || resourceLocationString.startsWith(input) || cleanedResourceLocation.startsWith(
                    input
                )
            ) return@sortedByDescending 2
            if (itemDescription.contains(input) || resourceLocationString.contains(input)) return@sortedByDescending 1
            0
        }.map { ModifyItemsEntry(it) { onSelect(it) } }.forEach(::addEntry)
    }
}

class ModifyItemsEntry(private val item: Item, private val onSelect: () -> Unit) :
    ObjectSelectionList.Entry<ModifyItemsEntry>() {
    private val itemLocationString = BuiltInRegistries.ITEM.getKey(item).run {
        val string = toString()
        if (string.length > 20) return@run "${string.take(17)}..."
        else return@run string
    }

    private val selectButton = Button.builder(screenComponent("modify.items.select")) {
        onSelect()
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
        guiGraphics.renderItem(ItemStack(item), x + INNER_PAD, y - 1)
        guiGraphics.drawString(
            internalMinecraft.font,
            itemLocationString,
            x + INNER_PAD + internalMinecraft.font.lineHeight * 2,
            y + height / 2 - internalMinecraft.font.lineHeight / 2,
            0xFFFFFF
        )

        selectButton.height = min(20, internalMinecraft.screen!!.height / 15)
        selectButton.setPosition(x + width - selectButton.width - INNER_PAD, y + height / 2 - selectButton.height / 2)
        selectButton.render(guiGraphics, mouseX, mouseY, delta)
    }

    override fun mouseClicked(d: Double, e: Double, i: Int): Boolean {
        if (selectButton.isMouseOver(d, e)) return selectButton.mouseClicked(d, e, i)
        return super.mouseClicked(d, e, i)
    }

    override fun getNarration(): Component {
        return item.narration
    }
}