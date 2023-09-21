package dev.nyon.autodrop.screen.archiveEntry

import dev.nyon.autodrop.config.reloadArchiveProperties
import dev.nyon.autodrop.config.saveConfig
import dev.nyon.autodrop.config.settings
import dev.nyon.autodrop.util.button
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ContainerObjectSelectionList
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class ArchiveEntryWidget(private val itemLocation: ResourceLocation, private val list: ArchiveEntryListWidget) :
    ContainerObjectSelectionList.Entry<ArchiveEntryWidget>() {

    private val removeButton = button(0, 0, 50, 20, Component.translatable("menu.autodrop.archiveentry.remove")) {
        settings.archives.first { it.name == list.archive }.items.remove(itemLocation)
        reloadArchiveProperties()
        saveConfig()
        list.refreshEntries()
    }

    override fun render(
        matrices: GuiGraphics,
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
        val item: Item? = kotlin.run {
            val rawItem = BuiltInRegistries.ITEM.get(itemLocation)
            if (rawItem == Items.AIR) return@run null
            else return@run rawItem
        }
        if (hovered) {
            matrices.fill(x - 1, y + entryHeight + 1, x + entryWidth - 5, y - 1, 0x90000000.toInt())

            removeButton.x = x + entryWidth - 60
            removeButton.y = y
            removeButton.render(matrices, mouseX, mouseY, tickDelta)
        }

        if (item != null) ItemIconWidget(item).render(matrices, x + 2, y + 2, tickDelta)

        matrices.drawString(
            minecraft.font,
            Component.literal(item?.description?.string ?: itemLocation.toString()),
            x + 30,
            y + 6,
            0x80FFFFFF.toInt(),
            false
        )
    }

    override fun children(): MutableList<out GuiEventListener> = mutableListOf(removeButton)

    override fun narratables(): MutableList<out NarratableEntry> = mutableListOf()
}


class ItemIconWidget(private val item: Item) : Renderable, GuiEventListener, NarratableEntry {

    override fun render(matrices: GuiGraphics, i: Int, j: Int, f: Float) {
        val minecraft = Minecraft.getInstance()
        val itemStack = ItemStack(item, 1)

        matrices.renderItem(itemStack, i, j)
        matrices.renderItemDecorations(minecraft.font, itemStack, i, j)
    }


    override fun updateNarration(narrationElementOutput: NarrationElementOutput) {}

    override fun narrationPriority(): NarratableEntry.NarrationPriority = NarratableEntry.NarrationPriority.NONE

    override fun setFocused(bl: Boolean) {}

    override fun isFocused(): Boolean = false
}