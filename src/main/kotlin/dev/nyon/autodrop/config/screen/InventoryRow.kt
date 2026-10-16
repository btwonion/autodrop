package dev.nyon.autodrop.config.screen

import dev.nyon.autodrop.config.Profile
import dev.nyon.autodrop.extensions.screenComponent
import dev.nyon.autodrop.planning.*
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.util.FormattedCharSequence
import net.minecraft.world.item.ItemStack

/** Unscaled size of the grid: armor and offhand on top, then the three main rows and the hotbar. */
private const val GRID_WIDTH = 162
private const val GRID_HEIGHT = 100

/** Largest whole-number scale at which the inventory fits, so slots grow with the available space. */
internal fun inventoryScale(width: Int, height: Int, max: Int = 3) =
    minOf(width / GRID_WIDTH, height / (GRID_HEIGHT + 8), max).coerceAtLeast(1)

/** Uses the InventoryMenu slot IDs that profiles store: 5–8 armor, 9–35 inventory, 36–44 hotbar, 45 offhand. */
private fun slotOffset(slot: Int): Pair<Int, Int> = when (slot) {
    in 5 .. 8 -> (slot - 5) * 18 to 0
    45 -> 8 * 18 to 0
    in 9 .. 35 -> (slot - 9) % 9 * 18 to 24 + (slot - 9) / 9 * 18
    else -> (slot - 36) * 18 to 82
}

private val EMPTY_SLOT_SPRITES = mapOf(
    5 to "helmet",
    6 to "chestplate",
    7 to "leggings",
    8 to "boots",
    45 to "shield"
).mapValues { sprite("container/slot/${it.value}") }

internal class InventoryRow(
    profile: Profile?,
    snapshot: () -> PlayerInventorySnapshot?,
    plan: () -> DropPlan,
    preview: Boolean,
    private val scale: Int,
    onSlot: (Int) -> Unit
) : NativeRow(GRID_HEIGHT * scale + 8) {
    private val slots = ((5 .. 8) + (9 .. 45)).associateWith { slot ->
        InventorySlotButton(slot, scale, profile, snapshot, plan, preview) { onSlot(slot) }.also { controls += it }
    }

    override fun extractContent(
        graphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        hovered: Boolean,
        delta: Float
    ) {
        val left = x + (width - GRID_WIDTH * scale) / 2
        slots.forEach { (slot, button) ->
            val (dx, dy) = slotOffset(slot)
            button.setPosition(left + dx * scale, y + 4 + dy * scale)
        }
        renderControls(graphics, mouseX, mouseY, delta)
    }
}

private class InventorySlotButton(
    private val slot: Int,
    private val scale: Int,
    private val profile: Profile?,
    private val snapshot: () -> PlayerInventorySnapshot?,
    private val plan: () -> DropPlan,
    private val preview: Boolean,
    action: () -> Unit
) : FlatButton(slotName(slot), action) {
    private var tooltipText = ""

    init {
        setSize(18 * scale, 18 * scale)
    }

    override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        val stack = snapshot()?.items?.get(slot) ?: ItemStack.EMPTY
        val decision = plan().at(slot)
        val protected = profile?.protectedSlots?.contains(slot) == true
        val marker = when {
            !preview -> if (protected) GREEN else null
            (decision?.dropCount ?: 0) > 0 -> RED
            decision?.reason == Reason.PROTECTED_SLOT || decision?.reason == Reason.KEEP_RULE -> GREEN
            else -> null
        }
        drawSlot(graphics, x, y, width)
        if (marker != null) tintSlot(graphics, x, y, width, marker)
        if (stack.isEmpty) EMPTY_SLOT_SPRITES[slot]?.let {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, it, x + scale, y + scale, 16 * scale, 16 * scale)
        } else drawItem(graphics, stack, x + scale, y + scale, scale)
        if (marker != null) frameSlot(graphics, x, y, width, marker)
        if (!preview && protected) drawLock(graphics, x, y, width)
        drawDecorations(graphics, stack, x, y, width)
        if (isHoveredOrFocused) {
            graphics.fill(x + scale, y + scale, x + width - scale, y + height - scale, 0x50FFFFFF)
            if (isFocused) graphics.outline(x, y, width, height, WHITE)
        }
        updateTooltip(stack, decision, protected)
    }

    private fun updateTooltip(stack: ItemStack, decision: SlotDecision?, protected: Boolean) {
        val tip = slotName(slot).copy().append(": ")
            .append(if (stack.isEmpty) screenComponent("slot.empty") else stack.hoverName)
        if (!preview) tip.append("\n").append(screenComponent(if (protected) "slot.protected" else "slot.unprotected"))
        if (decision != null) tip.append("\n")
            .append(screenComponent("preview.slot_count", decision.dropCount, decision.remaining)).append("\n")
            .append(reasonText(decision))
        if (decision?.method == DropMethod.SINGLE_ITEMS) tip.append("\n")
            .append(singleItemsWarning(decision).withStyle(ChatFormatting.GOLD))
        if (tip.string == tooltipText) return
        tooltipText = tip.string
        message = tip
        setTooltip(Tooltip.create(tip))
    }
}

/** Colour key for the inventory grid, centered under it. */
internal class LegendRow(private val entries: List<Pair<Int, Component>>) : NativeRow(16) {
    override fun extractContent(
        graphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        hovered: Boolean,
        delta: Float
    ) {
        val font = client.font
        val total = entries.sumOf { 14 + font.width(it.second) } + 12 * (entries.size - 1)
        var left = x + ((width - total) / 2).coerceAtLeast(0)
        entries.forEach { (color, label) ->
            tintSlot(graphics, left - 1, y + 2, 12, color)
            frameSlot(graphics, left - 1, y + 2, 12, color)
            graphics.text(font, label, left + 14, y + 4, MUTED)
            left += 14 + font.width(label) + 12
        }
    }
}

internal fun singleItemsWarning(decision: SlotDecision) = screenComponent("preview.single_items", decision.dropCount)

/** The reason for a planned slot, then a warning when its items can only be thrown one at a time. */
private fun decisionLines(decision: SlotDecision, width: Int): List<Pair<FormattedCharSequence, Int>> =
    wrap(reasonText(decision), width).map { it to MUTED } + if (decision.method == DropMethod.SINGLE_ITEMS) wrap(
        singleItemsWarning(decision),
        width
    ).map { it to GOLD } else emptyList()

/** One planned slot of the preview with its item, the amount and the reason. */
internal class PreviewDecisionRow(private val decision: SlotDecision, private val stack: ItemStack, wrapWidth: Int) :
    NativeRow(maxOf(26, 16 + decisionLines(decision, wrapWidth - 24).size * 10 + 2)) {
    private val lines = decisionLines(decision, wrapWidth - 24)
    override fun extractContent(
        graphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        hovered: Boolean,
        delta: Float
    ) {
        drawSlot(graphics, x, y + 3)
        graphics.item(stack, x + 1, y + 4)
        graphics.itemDecorations(client.font, stack, x + 1, y + 4)
        val title = slotName(decision.slot).copy().append(" · ")
            .append(screenComponent("preview.slot_count", decision.dropCount, decision.remaining))
        text(graphics, title, x + 24, y + 3, width - 24, if (decision.dropCount > 0) RED else WHITE)
        lines.forEachIndexed { index, (line, color) ->
            graphics.text(
                client.font,
                line,
                x + 24,
                y + 14 + index * 10,
                color
            )
        }
    }
}
