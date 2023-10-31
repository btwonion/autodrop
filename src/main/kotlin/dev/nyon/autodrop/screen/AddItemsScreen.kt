package dev.nyon.autodrop.screen

import dev.nyon.autodrop.config.reloadArchiveProperties
import dev.nyon.autodrop.config.settings
import dev.nyon.autodrop.screen.archiveEntry.ItemIconWidget
import dev.nyon.autodrop.util.button
import dev.nyon.konfig.config.saveConfig
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ContainerObjectSelectionList
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items

class AddItemsScreen(
    private val previous: Screen, private val archive: String, private val configScreen: ConfigScreen
) : Screen(Component.translatable("menu.autodrop.additem.name")) {

    private lateinit var nameInput: EditBox
    private lateinit var itemList: ItemList

    override fun init() {
        initWidgets()

        nameInput.setResponder {
            itemList.refreshEntries(it)
        }
        addRenderableWidget(itemList)
        addRenderableWidget(nameInput)
        addRenderableWidget(button(
            (this.width / 2) - this.width / 8, (this.height / 16) * 3, this.width / 4, 20, Component.translatable("menu.autodrop.additem.done")
        ) {
            onClose()
        })
    }

    override fun onClose() {
        minecraft?.setScreen(previous)
        configScreen.archiveEntryListWidget.refreshEntries()
    }

    override fun render(matrices: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        renderDirtBackground(matrices)
        super.render(matrices, mouseX, mouseY, delta)
        matrices.drawCenteredString(
            Minecraft.getInstance().font,
            Component.translatable("menu.autodrop.additem.entername"),
            this.width / 2,
            this.height / 16,
            0x80FFFFFF.toInt()
        )
    }

    private fun initWidgets() {
        nameInput = EditBox(
            Minecraft.getInstance().font,
            (this.width / 2) - this.width / 8,
            this.height / 8,
            this.width / 4,
            20,
            Component.translatable("menu.autodrop.additem.enteritemnameinput")
        )
        itemList = ItemList(archive, this.width, (this.height / 4) * 3, this.height / 4, this.height, 24, 0)
    }

    inner class ItemEntry(private val item: Item, private val archive: String) :
        ContainerObjectSelectionList.Entry<ItemEntry>() {

        private val addButton = button(0, 0, 50, 20, Component.translatable("menu.autodrop.additem.add")) {
            settings.archives.first { it.name == archive }.items.add(BuiltInRegistries.ITEM.getKey(item))
            reloadArchiveProperties()
            itemList.refreshEntries(nameInput.value, false)
            saveConfig(settings)
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
            if (hovered) {
                matrices.fill(x - 1, y + entryHeight + 1, x + entryWidth - 5, y - 1, 0x90000000.toInt())

                addButton.x = x + entryWidth - 60
                addButton.y = y
                addButton.render(matrices, mouseX, mouseY, tickDelta)
            }

            ItemIconWidget(item).render(matrices, x + 2, y + 2, tickDelta)

            matrices.drawString(
                minecraft.font,
                Component.literal(item.description.string),
                x + 30,
                y + 6,
                0x80FFFFFF.toInt(),
                false
            )
        }

        override fun children(): MutableList<out GuiEventListener> = mutableListOf(addButton)

        override fun narratables(): MutableList<out NarratableEntry> = mutableListOf()
    }

    inner class ItemList(
        private val archive: String, width: Int, height: Int, top: Int, bottom: Int, itemHeight: Int, left: Int
    ) : ContainerObjectSelectionList<ItemEntry>(Minecraft.getInstance(), width, height, top, bottom, itemHeight) {

        init {
            setLeftPos(left)
        }

        override fun getScrollbarPosition(): Int = rowLeft + rowWidth

        override fun getRowWidth(): Int = width - 40

        override fun render(matrices: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
            if (itemCount == 0) {
                matrices.drawCenteredString(
                    minecraft.font,
                    Component.translatable("menu.autodrop.additem.noitemsfound"),
                    x0 + (width / 2),
                    250,
                    0x80FFFFFF.toInt()
                )
                return
            }
            super.render(matrices, mouseX, mouseY, delta)
        }

        fun refreshEntries(input: String, scrollReset: Boolean = true) {
            clearEntries()
            if (input.isEmpty()) {
                BuiltInRegistries.ITEM.filter { item ->
                    !settings.archives.first { it.name == archive }.items.contains(
                        BuiltInRegistries.ITEM.getKey(item)
                    ) && item != Items.AIR
                }.sortedBy { it.description.string }.forEach { addEntry(ItemEntry(it, archive)) }
                if (scrollReset) scrollAmount = 0.0
                return
            }

            BuiltInRegistries.ITEM.filter {
                Item.getId(it).toString().startsWith(input, true) || Item.getId(it).toString()
                    .contains(input, true) || it.description.string.startsWith(
                    input, true
                ) || it.description.string.equals(
                    input, true
                ) || it.description.string.contains(input, true) || BuiltInRegistries.ITEM.getKey(it).toString()
                    .contains(input)
            }.filter { item ->
                !settings.archives.first { it.name == archive }.items.contains(
                    BuiltInRegistries.ITEM.getKey(item)
                )
            }.forEach { addEntry(ItemEntry(it, archive)) }

            if (scrollReset) scrollAmount = 0.0
        }
    }
}