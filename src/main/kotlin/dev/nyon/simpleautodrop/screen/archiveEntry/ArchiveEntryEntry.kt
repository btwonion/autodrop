package dev.nyon.simpleautodrop.screen.archiveEntry

import com.mojang.blaze3d.vertex.PoseStack
import dev.nyon.simpleautodrop.config.reloadCachedIds
import dev.nyon.simpleautodrop.config.saveConfig
import dev.nyon.simpleautodrop.config.settings
import dev.nyon.simpleautodrop.util.button
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiComponent
import net.minecraft.client.gui.components.ContainerObjectSelectionList
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

class ArchiveEntryWidget(private val item: Item, private val list: ArchiveEntryListWidget) :
    ContainerObjectSelectionList.Entry<ArchiveEntryWidget>() {

    private val removeButton = button(0, 0, 50, 20, Component.literal("Remove")) {
        settings.items[list.archive]?.remove(item)
        reloadCachedIds()
        saveConfig()
        list.refreshEntries()
    }

    override fun render(
        matrices: PoseStack,
        index: Int,
        y: Int,
        x: Int,
        entryWidth: Int,
        entryHeight: Int,
        mouseX: Int,
        mouseY: Int,
        hovered: Boolean,
        tickDelta: Float
    ) {
        val minecraft = Minecraft.getInstance()
        if (hovered) {
            GuiComponent.fill(matrices, x - 1, y + entryHeight + 1, x + entryWidth - 5, y - 1, 0x90000000.toInt())

            removeButton.x = x + entryWidth - 60
            removeButton.y = y
            removeButton.render(matrices, mouseX, mouseY, tickDelta)
        }

        ItemIconWidget(item).render(matrices, x + 2, y + 2, tickDelta)

        minecraft.font.draw(
            matrices, Component.literal(item.description.string), x + 30.toFloat(), y + 6.toFloat(), 0x80FFFFFF.toInt()
        )
    }

    override fun children(): MutableList<out GuiEventListener> = mutableListOf(removeButton)

    override fun narratables(): MutableList<out NarratableEntry> = mutableListOf()
}


class ItemIconWidget(private val item: Item) : Renderable, GuiEventListener, GuiComponent(), NarratableEntry {

    override fun render(poseStack: PoseStack, i: Int, j: Int, f: Float) {
        val minecraft = Minecraft.getInstance()
        val itemRenderer = minecraft.itemRenderer
        val itemStack = ItemStack(item, 1)

        this.blitOffset = 100
        itemRenderer.blitOffset = 100.0f
        itemRenderer.renderGuiItem(itemStack, i, j)
        itemRenderer.renderGuiItemDecorations(minecraft.font, itemStack, i, j)
        itemRenderer.blitOffset = 0.0f
        this.blitOffset = 0
    }


    override fun updateNarration(narrationElementOutput: NarrationElementOutput) {}

    override fun narrationPriority(): NarratableEntry.NarrationPriority = NarratableEntry.NarrationPriority.NONE
}