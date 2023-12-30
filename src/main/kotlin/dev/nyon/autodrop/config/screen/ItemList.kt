package dev.nyon.autodrop.config.screen

import dev.nyon.autodrop.config.settings
import dev.nyon.autodrop.minecraft
import dev.nyon.autodrop.util.button
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.text.Text

/**
 * @author btwonion
 * @since 19/12/2023
 */
class ItemList(
    x: Int, y: Int, width: Int, height: Int, private val left: Int, private val archiveName: String, archiveReloadCallback: () -> Unit
) : AlwaysSelectedEntryListWidget<ItemListEntry>(
    minecraft, x, y, width, height
) {
    private val itemListEntryPlaceholder =
        ItemListEntryPlaceholder(archiveName, { loadEntries() }, archiveReloadCallback)

    override fun getRowLeft(): Int {
        return left
    }

    fun loadEntries() {
        clearEntries()

        settings.archives.find { it.name == archiveName }?.items?.forEach {
            addEntry(ItemListEntryItem(archiveName, Registries.ITEM.get(it)) {
                loadEntries()
            })
        }

        addEntryToTop(itemListEntryPlaceholder)
    }
}

abstract class ItemListEntry : AlwaysSelectedEntryListWidget.Entry<ItemListEntry>()

class ItemListEntryItem(
    private val archiveName: String, private val item: Item, private val itemsReloadCallback: () -> Unit
) : ItemListEntry() {
    override fun render(
        gui: DrawContext,
        index: Int,
        top: Int,
        left: Int,
        width: Int,
        height: Int,
        mouseX: Int,
        mouseY: Int,
        hovering: Boolean,
        partialTick: Float
    ) {
        val padding = width / 12
        val topBottomPadding = height / 10

        // Item icon
        gui.drawItem(item.defaultStack, left + padding, top + topBottomPadding)

        // Item name
        gui.drawText(minecraft.textRenderer, item.name, left + width / 6, top + topBottomPadding, 0xFFFFFF, false)

        // Delete button
        val buttonWidth = width / 6
        button(
            left + width - buttonWidth - padding,
            top + topBottomPadding,
            buttonWidth,
            height - 2 * topBottomPadding,
            Text.translatable("menu.autodrop.items.deleteitem.button")
        ) {
            settings.archives.find { it.name == archiveName }?.items?.remove(Registries.ITEM.getId(item))
            itemsReloadCallback()
        }
    }

    override fun getNarration(): Text {
        return Text.translatable("menu.autodrop.archives.overview.title")
    }
}

class ItemListEntryPlaceholder(
    private val archiveName: String, val itemsReloadCallback: () -> Unit, private val archiveReloadCallback: () -> Unit
) : ItemListEntry() {
    override fun render(
        guiGraphics: DrawContext,
        index: Int,
        top: Int,
        left: Int,
        width: Int,
        height: Int,
        mouseX: Int,
        mouseY: Int,
        hovering: Boolean,
        partialTick: Float
    ) {
        val padding = width / 10
        val topBottomPadding = height / 10
        val buttonWidth = (width - 3 * padding) / 2
        val buttonHeight = (height - 3 * topBottomPadding) / 2

        // Add items button - top full
        button(
            left + padding,
            top + topBottomPadding,
            width - 2 * padding,
            buttonHeight,
            Text.translatable("menu.autodrop.items.additems.button")
        ) {
            TODO("add items screen")
            itemsReloadCallback()
        }

        // Set locked slots button - bottom half left
        button(
            left + padding,
            top + 2 * padding + buttonHeight,
            buttonHeight,
            buttonWidth,
            Text.translatable("menu.autodrop.items.setlockedslots")
        ) {
            TODO("set locked slots screen")
        }.render(guiGraphics, mouseX, mouseY, partialTick)

        // Delete archive button - bottom half right
        button(
            left + 2 * padding + buttonWidth,
            top + 2 * padding + buttonHeight,
            buttonHeight,
            buttonWidth,
            Text.translatable("menu.autodrop.archives.delete").withColor(0xFF0000)
        ) {
            settings.archives.removeIf { it.name == archiveName }
            settings.activeArchives.removeIf { it == archiveName }
            archiveReloadCallback()
        }.render(guiGraphics, mouseX, mouseY, partialTick)
    }

    override fun getNarration(): Text {
        return Text.translatable("menu.autodrop.archives.overview.title")
    }
}