package dev.nyon.autodrop.config.screen.modify

import dev.nyon.autodrop.AutoDrop
import dev.nyon.autodrop.config.screen.root.INNER_PAD
import dev.nyon.autodrop.config.screen.root.OUTER_PAD
import dev.nyon.autodrop.extensions.narration
import dev.nyon.autodrop.extensions.screenComponent
import dev.nyon.autodrop.extensions.screenHeight
import dev.nyon.autodrop.extensions.screenWidth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.ObjectSelectionList
//? if >1.21.8
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds
import dev.nyon.autodrop.AutoDrop.minecraft as internalMinecraft

class ModifyItemsWidget(var input: String, private val onSelect: Item.() -> Unit) :
    ObjectSelectionList<ModifyItemsEntry>(
        internalMinecraft,
        screenWidth / 2,
        screenHeight / 6,
        OUTER_PAD + INNER_PAD * 2 + internalMinecraft.font.lineHeight + 20,
        internalMinecraft.font.lineHeight + 2 * INNER_PAD
    ) {
    override fun getX(): Int {
        return screenWidth / 4
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
        width = screenWidth / 2
        height = screenHeight / 6
        y = OUTER_PAD + INNER_PAD * 2 + internalMinecraft.font.lineHeight + 20
        super.renderWidget(guiGraphics, i, j, f)
    }

    fun refreshEntries() {
        /*? if <1.21.4 {*/ /*scrollAmount = 0.0 *//*?} else {*/ setScrollAmount(0.0) /*?}*/
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

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        children().forEach { it.mouseMoved(mouseX, mouseY) }
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

    //? if >1.21.8 {
    override fun renderContent(graphics: GuiGraphics, mouseX: Int, mouseY: Int, hovered: Boolean, delta: Float) {
        renderAdItems(graphics, mouseX, mouseY, delta)
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
        renderAdItems(guiGraphics, mouseX, mouseY, delta, x, y, width, height)
    }
    *///?}

    private fun renderAdItems(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float/*? if <=1.21.8 {*//*, x: Int, y: Int, width: Int, height: Int*//*?}*/) {
        guiGraphics.renderItem(ItemStack(item), x + INNER_PAD, y - 1)
        guiGraphics.drawString(
            internalMinecraft.font,
            itemLocationString,
            x + INNER_PAD + internalMinecraft.font.lineHeight * 2,
            y + height / 2 - internalMinecraft.font.lineHeight / 2,
            0xFFFFFFFF.toInt()
        )

        selectButton.height = min(20, screenHeight / 15)
        selectButton.setPosition(x + width - selectButton.width - INNER_PAD, y + height / 2 - selectButton.height / 2)
        selectButton.render(guiGraphics, mouseX, mouseY, delta)
    }

    //? if >1.21.8 {
    override fun mouseClicked(mouseButtonEvent: MouseButtonEvent, bl: Boolean): Boolean {
        if (selectButton.isMouseOver(mouseButtonEvent.x, mouseButtonEvent.y)) return selectButton.mouseClicked(mouseButtonEvent, bl)
        return super.mouseClicked(mouseButtonEvent, bl)

    }
    //?} else {
    /*override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (selectButton.isMouseOver(mouseX, mouseY)) return selectButton.mouseClicked(mouseX, mouseY, button)
        return super.mouseClicked(mouseX, mouseY, button)
    }
    *///?}

    private var job: Job? = null
    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        selectButton.visible = true
        job = AutoDrop.mcScope.launch {
            delay(1.seconds)
            selectButton.visible = false
        }
        return super.mouseMoved(mouseX, mouseY)
    }

    override fun getNarration(): Component {
        return item.narration
    }
}