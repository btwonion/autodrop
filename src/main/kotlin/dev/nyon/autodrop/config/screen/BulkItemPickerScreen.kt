package dev.nyon.autodrop.config.screen

import dev.nyon.autodrop.config.ConfigDraft
import dev.nyon.autodrop.config.Profile
import dev.nyon.autodrop.config.ProfileRule
import dev.nyon.autodrop.extensions.screenComponent
import dev.nyon.autodrop.planning.MinecraftPlanner
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractButton
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.input.KeyEvent
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.util.*

/** Creative-inventory style picker: search, then click item slots to select several items at once. */
class BulkItemPickerScreen(
    private val parent: ProfileWorkspaceScreen,
    draft: ConfigDraft,
    private val profile: Profile
) : DraftScreen(screenComponent("picker.title", profile.name), draft) {
    private class Choice(val id: String?, val name: Component, val stack: ItemStack) {
        /** Lowercased name and ID, computed once so typing only has to compare strings. */
        val search = (name.string + "\n" + (id ?: "*")).lowercase(Locale.ROOT)
    }

    private val selection = linkedSetOf<String?>()

    /** Slots are built once per layout and reused across searches instead of on every keystroke. */
    private val slots = mutableMapOf<String?, PickerSlot>()
    private var search: EditBox? = null

    // Registry order groups related items (stone variants, wood sets) like the creative inventory does.
    private val catalog by lazy {
        BuiltInRegistries.ITEM.filter { it != Items.AIR }.map {
            val id = BuiltInRegistries.ITEM.getKey(it).toString()
            Choice(id, localizedItemName(it), itemStack(id))
        }
    }
    private val anyItem = Choice(null, screenComponent("any_item"), ItemStack.EMPTY)
    private var query = ""
    private var inInventory = false
    private var shown = 0
    private lateinit var list: RowList
    private var add: AbstractButton? = null
    private var area = 0
    private var controls = 0
    private var columns = 9
    private var scale = 1

    override fun init() {
        scale = if (width >= 600 && height >= 380) 2 else 1
        slots.clear()
        val cell = 18 * scale
        area = minOf(width - 16, 16 * cell + 10)
        columns = (area - 10) / cell
        val start =
            (width - area) / 2 // The search and filter buttons may be wider than a small grid, so their labels fit.
        controls = minOf(width - 16, maxOf(area, 390))
        val controlStart = (width - controls) / 2
        search = addRenderableWidget(
            EditBox(
                font,
                controlStart,
                HEADER + 6,
                controls,
                20,
                screenComponent("picker.search")
            )
        ).also {
            it.setMaxLength(160); it.value = query; it.setHint(screenComponent("picker.search"))
            it.setResponder { text -> query = text; refresh() }
            setInitialFocus(it)
        }
        val inventory = MinecraftPlanner.snapshot()
        val third = (controls - 8) / 3
        button(
            screenComponent("picker.inventory", screenComponent(if (inInventory) "enabled" else "disabled")),
            controlStart,
            HEADER + 30,
            third
        ) {
            inInventory = !inInventory; rebuildKeepingFocus()
        }.also {
            it.active = inventory != null
            it.setTooltip(Tooltip.create(screenComponent(if (inventory == null) "status.no_world" else "picker.inventory.description")))
        }
        button(
            screenComponent("picker.select_results"),
            controlStart + third + 4,
            HEADER + 30,
            third
        ) { // The wildcard is never part of a bulk selection; it has to be picked on purpose.
            filtered().filter { it.id != null && !already(it.id) }.forEach { selection += it.id }; updateCount()
        }.setTooltip(Tooltip.create(screenComponent("picker.select_results.description")))
        button(
            screenComponent("picker.clear"),
            controlStart + (third + 4) * 2,
            HEADER + 30,
            controls - (third + 4) * 2
        ) { selection.clear(); updateCount() }
        list = addRenderableWidget(RowList(start, HEADER + 56, area, height - FOOTER - 18 - (HEADER + 56)))
        add = footerButtons(screenComponent("picker.add", selection.size)) {
            selection.filter { id -> !already(id) }.forEach { profile.entries += ProfileRule(type = it) }
            if (selection.isNotEmpty()) keepListChanged(profile)
            show(parent)
        }
        refresh()
    }

    private fun already(id: String?) = profile.entries.any { it.type == id }
    private fun filtered(): List<Choice> {
        val types = MinecraftPlanner.snapshot()?.stacks?.map { it.type }?.toSet().orEmpty()
        val text = query.trim().lowercase(Locale.ROOT)
        val any = text.isNotEmpty() && anyItem.search.contains(text)
        return (if (any && !inInventory) listOf(anyItem) else emptyList()) + catalog.filter { choice ->
            (!inInventory || choice.id in types) && (text.isEmpty() || choice.search.contains(text))
        }
    }

    private fun refresh() {
        if (!::list.isInitialized) return
        val choices = filtered()
        shown = choices.size
        val rowWidth = list.rowWidth
        list.rows(
            if (choices.isEmpty()) listOf(
                TextRow(
                    screenComponent("picker.no_results"),
                    rowWidth,
                    centered = true
                )
            ) else choices.chunked(columns).map { row ->
                SlotGridRow(row.map { choice ->
                    slots.getOrPut(choice.id) {
                        PickerSlot(
                            choice.id,
                            choice.name,
                            choice.stack,
                            scale,
                            { choice.id in selection },
                            already(choice.id)
                        ) {
                            if (!selection.add(choice.id)) selection.remove(choice.id)
                            updateCount()
                        }
                    }
                }, 18 * scale)
            })
        list.setScrollAmount(0.0)
        updateCount()
    }

    private fun updateCount() {
        add?.message = screenComponent("picker.add", selection.size)
        add?.active = selection.isNotEmpty()
    }

    override fun keyPressed(event: KeyEvent): Boolean { // Escape in a filled search box clears the search; a second Escape closes the picker.
        val field = search
        if (event.isEscape && field != null && field.isFocused && field.value.isNotEmpty()) {
            field.value = ""; return true
        }
        return super.keyPressed(event)
    }

    override fun onClose() {
        show(parent)
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        super.extractRenderState(graphics, mouseX, mouseY, delta)
        line(
            graphics,
            screenComponent("picker.status", shown, selection.size),
            (width - area) / 2,
            height - FOOTER - 13,
            area,
            MUTED,
            mouseX,
            mouseY,
            centered = true
        )
    }
}

/** One row of the item grid; slots touch like in an inventory. */
internal class SlotGridRow(slots: List<PickerSlot>, private val cell: Int) : NativeRow(cell) {
    init {
        controls += slots
    }

    override fun extractContent(
        graphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        hovered: Boolean,
        delta: Float
    ) {
        val columns = width / cell
        val left = x + (width - columns * cell) / 2
        controls.forEachIndexed { index, slot -> slot.setPosition(left + index * cell, y) }
        renderControls(graphics, mouseX, mouseY, delta)
    }
}

internal class PickerSlot(
    id: String?,
    name: Component,
    private val stack: ItemStack,
    private val scale: Int,
    private val selected: () -> Boolean,
    private val already: Boolean,
    action: () -> Unit
) : FlatButton(name, action) {
    private val wildcard = id == null

    init {
        setSize(18 * scale, 18 * scale)
        active = !already
        val tip = name.copy().append("\n").append(Component.literal(id ?: "*").withStyle { it.withColor(MUTED) })
        if (wildcard) tip.append("\n").append(screenComponent("any_item.description"))
        if (already) tip.append("\n").append(screenComponent("picker.already"))
        setTooltip(Tooltip.create(tip))
    }

    override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        val chosen = selected()
        drawSlot(graphics, x, y, width)
        if (chosen) tintSlot(graphics, x, y, width, GREEN)
        if (wildcard) drawGlyph(graphics, "*", x, y, width, GOLD)
        else if (stack.isEmpty) drawGlyph(graphics, "?", x, y, width, MUTED)
        else drawItem(graphics, stack, x + scale, y + scale, scale)
        if (already) graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xB0101010.toInt())
        if (chosen) {
            frameSlot(graphics, x, y, width, GREEN); drawCheck(graphics, x, y, width)
        }
        if (isHoveredOrFocused && !already) {
            graphics.fill(x + scale, y + scale, x + width - scale, y + height - scale, 0x50FFFFFF)
            if (isFocused) graphics.outline(x, y, width, height, WHITE)
        }
    }
}
