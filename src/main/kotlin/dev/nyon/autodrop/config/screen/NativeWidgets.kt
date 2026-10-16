package dev.nyon.autodrop.config.screen

import com.mojang.logging.LogUtils
import dev.nyon.autodrop.AutoDrop
import dev.nyon.autodrop.config.*
import dev.nyon.autodrop.extensions.VanillaRegistryAccess
import dev.nyon.autodrop.extensions.identifier
import dev.nyon.autodrop.extensions.openScreen
import dev.nyon.autodrop.extensions.screenComponent
import dev.nyon.autodrop.planning.PredicateCache
import dev.nyon.autodrop.planning.SlotDecision
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.*
import net.minecraft.client.gui.components.events.ContainerEventHandler
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.InputWithModifiers
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.util.FormattedCharSequence
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

internal val client get() = AutoDrop.minecraft
internal const val WHITE = -1
internal const val MUTED = -6250336
internal const val GOLD = -171
internal const val GREEN = 0xFF55DD55.toInt()
internal const val RED = 0xFFFF5555.toInt()
/** Header and footer heights of vanilla's HeaderAndFooterLayout. */
internal const val HEADER = 33
internal const val FOOTER = 33

internal fun sprite(path: String): Identifier = Identifier.withDefaultNamespace(path)
private val SLOT_SPRITE = sprite("container/slot")
private val LOCK_SPRITE = sprite("container/cartography_table/locked")
private val CHECK_SPRITE = sprite("icon/checkmark")
private val LIST_BACKGROUND = sprite("textures/gui/menu_list_background.png")
private val INWORLD_LIST_BACKGROUND = sprite("textures/gui/inworld_menu_list_background.png")

/** Base for all config screens: a vanilla header with the title, a darker body, and a footer for actions. */
abstract class DraftScreen(title: Component, val draft: ConfigDraft) : Screen(title) {
    init { prepareItemComponents() }
    override fun added() { super.added(); AutoDrop.setEditing(true) }
    protected fun show(screen: Screen?) { client.openScreen(screen) }
    /**
     * A predicate's error, if any. Predicates the saved config already contains are not checked again, so one that
     * needs another world's data never blocks unrelated changes; dropping still pauses if an enabled one fails.
     */
    protected fun predicateError(predicate: String): String? =
        if (draft.isSavedPredicate(predicate)) null else PredicateCache.compile(predicate).exceptionOrNull()?.message
    protected fun rebuildKeepingFocus() {
        val path = mutableListOf<Int>()
        var container: ContainerEventHandler = this
        while (true) {
            val focused = container.focused ?: break
            val index = container.children().indexOfFirst { it === focused }
            if (index < 0) break
            path += index
            container = focused as? ContainerEventHandler ?: break
        }
        rebuildWidgets()
        container = this
        for (index in path) {
            val child = container.children().getOrNull(index) ?: break
            container.setFocused(child)
            container = child as? ContainerEventHandler ?: break
        }
    }
    protected fun button(label: Component, x: Int, y: Int, width: Int, action: () -> Unit): Button =
        addRenderableWidget(Button.builder(label) { action() }.bounds(x, y, width, 20).build())
    /** Adds Cancel and a confirming action centered in the footer, like vanilla dialogs; [danger] makes it red. */
    protected fun footerButtons(confirm: Component, danger: Boolean = false, action: () -> Unit): AbstractButton {
        val each = minOf(150, (width - 20) / 2)
        button(screenComponent("cancel"), width / 2 - each - 4, height - 26, each) { onClose() }
        if (!danger) return button(confirm, width / 2 + 4, height - 26, each, action)
        return addRenderableWidget(DangerButton(confirm, action)).also { it.setRectangle(each, 20, width / 2 + 4, height - 26) }
    }
    /** Paragraphs centered in the body, for dialogs without a list. */
    protected fun paragraphs(graphics: GuiGraphicsExtractor, paragraphs: List<Pair<Component, Int>>) {
        val wrapped = paragraphs.map { (text, color) -> wrap(text, minOf(width - 16, 340)) to color }
        val total = wrapped.sumOf { it.first.size * 10 } + 8 * (wrapped.size - 1)
        var top = HEADER + (height - HEADER - FOOTER - total) / 2
        wrapped.forEach { (lines, color) ->
            lines.forEach { graphics.centeredText(font, it, width / 2, top, color); top += 10 }
            top += 8
        }
    }
    /** Draws a single line, cut to [maxWidth] with the full text as tooltip when it does not fit. */
    protected fun line(graphics: GuiGraphicsExtractor, text: Component, x: Int, y: Int, maxWidth: Int, color: Int, mouseX: Int, mouseY: Int, centered: Boolean = false) {
        val fits = font.width(text) <= maxWidth
        val shown = if (fits) text.string else font.plainSubstrByWidth(text.string, (maxWidth - font.width("…")).coerceAtLeast(1)) + "…"
        val left = if (centered) x + (maxWidth - font.width(shown)) / 2 else x
        graphics.text(font, shown, left, y, color)
        if (!fits && mouseX in left..left + font.width(shown) && mouseY in y - 1..y + font.lineHeight)
            graphics.setTooltipForNextFrame(font, text, mouseX, mouseY)
    }
    override fun extractBackground(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        super.extractBackground(graphics, mouseX, mouseY, delta)
        val inWorld = client.level != null
        Screen.extractMenuBackgroundTexture(graphics, if (inWorld) INWORLD_LIST_BACKGROUND else LIST_BACKGROUND, 0, HEADER, 0f, 0f, width, height - HEADER - FOOTER)
        graphics.blit(RenderPipelines.GUI_TEXTURED, if (inWorld) Screen.INWORLD_HEADER_SEPARATOR else Screen.HEADER_SEPARATOR, 0, HEADER - 2, 0f, 0f, width, 2, 32, 2)
        graphics.blit(RenderPipelines.GUI_TEXTURED, if (inWorld) Screen.INWORLD_FOOTER_SEPARATOR else Screen.FOOTER_SEPARATOR, 0, height - FOOTER, 0f, 0f, width, 2, 32, 2)
    }
    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        super.extractRenderState(graphics, mouseX, mouseY, delta)
        graphics.centeredText(font, title, width / 2, (HEADER - font.lineHeight) / 2, WHITE)
    }
}

/** A scrolling column of rows. The screen draws the shared body background, so the list itself stays transparent. */
internal class RowList(x: Int, y: Int, width: Int, height: Int) :
    ContainerObjectSelectionList<NativeRow>(client, width, height.coerceAtLeast(20), y, 24) {
    init { setX(x); centerListVertically = false }
    override fun getRowLeft() = x
    override fun getRowWidth() = width - 10
    override fun scrollBarX() = right - scrollbarWidth()
    override fun extractListBackground(graphics: GuiGraphicsExtractor) {}
    override fun extractListSeparators(graphics: GuiGraphicsExtractor) {}
    fun rows(rows: List<NativeRow>) {
        setFocused(null); setSelected(null); clearEntries()
        rows.forEach { addEntry(it, it.rowHeight) }
    }
}

internal abstract class NativeRow(val rowHeight: Int) : ContainerObjectSelectionList.Entry<NativeRow>() {
    protected val controls = mutableListOf<AbstractWidget>()
    override fun children(): List<GuiEventListener> = controls
    override fun narratables(): List<NarratableEntry> = controls
    protected fun renderControls(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        controls.forEach { it.extractRenderState(graphics, mouseX, mouseY, delta) }
    }
    protected fun text(graphics: GuiGraphicsExtractor, text: Component, x: Int, y: Int, width: Int, color: Int = WHITE) {
        graphics.text(client.font, ellipsize(text, width), x, y, color)
    }
}

internal fun wrap(text: Component, width: Int): List<FormattedCharSequence> = client.font.split(text, width.coerceAtLeast(1))
internal fun ellipsize(text: Component, width: Int): String {
    val font = client.font
    if (font.width(text) <= width) return text.string
    return font.plainSubstrByWidth(text.string, (width - font.width("…")).coerceAtLeast(1)) + "…"
}
private fun lines(graphics: GuiGraphicsExtractor, lines: List<FormattedCharSequence>, x: Int, y: Int, color: Int, center: Int? = null) {
    lines.forEachIndexed { index, line ->
        if (center == null) graphics.text(client.font, line, x, y + index * 10, color)
        else graphics.centeredText(client.font, line, center, y + index * 10, color)
    }
}

/** Wrapped text; [wrapWidth] must be the list's row width. */
internal class TextRow(message: Component, wrapWidth: Int, private val color: Int = MUTED, private val centered: Boolean = false) :
    NativeRow(wrap(message, wrapWidth).size * 10 + 6) {
    private val lines = wrap(message, wrapWidth)
    override fun extractContent(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, hovered: Boolean, delta: Float) {
        lines(graphics, lines, x, y + 3, color, if (centered) x + width / 2 else null)
    }
}

/** A section title with a faint rule underneath. */
internal class HeadingRow(private val label: Component) : NativeRow(22) {
    override fun extractContent(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, hovered: Boolean, delta: Float) {
        text(graphics, label, x, y + 8, width, GOLD)
        graphics.fill(x, y + 19, x + width, y + 20, 0x40FFFFFF)
    }
}

internal fun rowButton(label: Component, tooltip: Component? = null, action: () -> Unit): Button =
    Button.builder(label) { action() }.build().also { if (tooltip != null) it.setTooltip(Tooltip.create(tooltip)) }

/** Buttons sharing one row evenly. */
internal class ButtonRow(vararg buttons: Button) : NativeRow(24) {
    init { controls += buttons }
    override fun extractContent(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, hovered: Boolean, delta: Float) {
        val each = (width - 4 * (controls.size - 1)) / controls.size
        controls.forEachIndexed { index, button -> button.setRectangle(each, 20, x + index * (each + 4), y + 2) }
        renderControls(graphics, mouseX, mouseY, delta)
    }
}

/** An option button with its explanation directly underneath, like a labelled vanilla option. */
internal class OptionRow(private val control: AbstractWidget, description: Component, wrapWidth: Int) :
    NativeRow(24 + wrap(description, wrapWidth).size * 10 + 8) {
    private val lines = wrap(description, wrapWidth)
    init { controls += control }
    override fun extractContent(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, hovered: Boolean, delta: Float) {
        control.setRectangle(width, 20, x, y + 2)
        renderControls(graphics, mouseX, mouseY, delta)
        lines(graphics, lines, x, y + 26, MUTED)
    }
}

/**
 * A labelled text field with an optional explanation. Inline fields put a short field to the right of the label,
 * which suits numbers; otherwise the field spans the row below its label.
 */
internal class FieldRow(
    private val label: Component, value: String, description: Component?, wrapWidth: Int,
    maxLength: Int = 12, private val inline: Boolean = false, onChange: (String) -> Unit
) : NativeRow((if (inline) 24 else 34) + (description?.let { wrap(it, wrapWidth).size * 10 + 2 } ?: 0) + 6) {
    private val lines = description?.let { wrap(it, wrapWidth) }.orEmpty()
    private var laidOutWidth = -1
    val field = EditBox(client.font, 0, 0, 100, 20, label).also {
        it.setMaxLength(maxLength); it.value = value; it.moveCursorToStart(false); it.setResponder(onChange)
        controls += it
    }
    override fun extractContent(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, hovered: Boolean, delta: Float) {
        val fieldWidth = if (inline) minOf(90, width / 3) else width
        if (inline) {
            text(graphics, label, x, y + 8, width - fieldWidth - 8)
            field.setRectangle(fieldWidth, 20, x + width - fieldWidth, y + 2)
        } else {
            text(graphics, label, x, y + 2, width)
            field.setRectangle(fieldWidth, 20, x, y + 12)
        }
        // Text set before layout scrolls to its end; show the start once the real width is known.
        if (laidOutWidth != fieldWidth) { laidOutWidth = fieldWidth; if (!field.isFocused) field.moveCursorToStart(false) }
        renderControls(graphics, mouseX, mouseY, delta)
        lines(graphics, lines, x, y + if (inline) 25 else 35, MUTED)
    }
}

/** A borderless button for custom-drawn rows; the whole area highlights on hover and focus. */
internal abstract class FlatButton(message: Component, private val action: () -> Unit) : AbstractButton(0, 0, 0, 0, message) {
    override fun onPress(input: InputWithModifiers) { action() }
    override fun updateWidgetNarration(output: NarrationElementOutput) { defaultButtonNarrationText(output) }
    protected fun highlight(graphics: GuiGraphicsExtractor) {
        if (isHoveredOrFocused) {
            graphics.fill(x, y, x + width, y + height, 0x20FFFFFF)
            if (isFocused) graphics.outline(x, y, width, height, WHITE)
        }
    }
}

/** Vanilla sprite button (such as the cross used to remove entries). */
internal class SpriteButton(private val sprite: Identifier, private val highlighted: Identifier, tooltip: Component, action: () -> Unit) :
    FlatButton(tooltip, action) {
    init { setTooltip(Tooltip.create(tooltip)) }
    override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, if (isHoveredOrFocused) highlighted else sprite, x, y, width, height)
    }
}

/** Vanilla checkbox without a label. */
internal class CheckboxButton(private val checked: () -> Boolean, tooltip: Component, action: () -> Unit) : FlatButton(tooltip, action) {
    init { setTooltip(Tooltip.create(tooltip)) }
    override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        val name = "widget/checkbox" + (if (checked()) "_selected" else "") + (if (isHoveredOrFocused) "_highlighted" else "")
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite(name), x, y, width, height)
    }
}

/** Vanilla tab height; the sprites are drawn for it. */
internal const val TAB_HEIGHT = 24

/** A tab in the vanilla style; the selected one is raised and underlined. */
internal class TabWidget(label: Component, private val selected: Boolean, action: () -> Unit) : FlatButton(label, action) {
    override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        val name = "widget/tab" + (if (selected) "_selected" else "") + (if (isHoveredOrFocused) "_highlighted" else "")
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite(name), x, y, width, height)
        val font = client.font
        val label = ellipsize(message, width - 8)
        // An unselected tab's sprite leaves its top 4 pixels empty, so center the label in the visible part.
        val top = if (selected) y else y + 4
        graphics.centeredText(font, label, x + width / 2, (top + y + height - font.lineHeight) / 2 + 1, if (selected) WHITE else MUTED)
        if (selected) {
            val line = minOf(font.width(label), width - 4)
            graphics.fill(x + (width - line) / 2, y + height - 3, x + (width + line) / 2, y + height - 2, WHITE)
        }
    }
}

/** A vanilla button tinted red, for actions that delete or discard something. */
internal class DangerButton(label: Component, action: () -> Unit) : FlatButton(label, action) {
    override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        val name = when { !active -> "widget/button_disabled"; isHoveredOrFocused -> "widget/button_highlighted"; else -> "widget/button" }
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite(name), x, y, width, height, if (isHoveredOrFocused) 0xFFFF8080.toInt() else 0xFFE04848.toInt())
        graphics.centeredText(client.font, ellipsize(message, width - 8), x + width / 2, y + (height - 8) / 2, if (active) WHITE else MUTED)
    }
}

private var itemComponentsPrepared = false

/**
 * Item prototypes only receive their components when a world loads, so outside a world no item could render.
 * Bind the vanilla defaults once for the config screens; joining a world binds that world's components again.
 */
private fun prepareItemComponents() {
    if (itemComponentsPrepared) return
    itemComponentsPrepared = true
    if (Items.STONE.builtInRegistryHolder().areComponentsBound()) return
    runCatching {
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(VanillaRegistryAccess.createVanillaRegistryAccess())
            .filter { it.key() == Registries.ITEM }.forEach { it.apply() }
    }.onFailure { LogUtils.getLogger().warn("Could not prepare item icons outside a world", it) }
}

private fun registryItem(id: String?): Item? = id?.let { identifier(it) }?.let { BuiltInRegistries.ITEM.getOptional(it).orElse(null) }

internal fun itemStack(id: String?): ItemStack {
    val item = registryItem(id) ?: return ItemStack.EMPTY
    if (!item.builtInRegistryHolder().areComponentsBound()) return ItemStack.EMPTY
    return item.defaultInstance
}

internal fun localizedItemName(item: Item): Component = if (item.builtInRegistryHolder().areComponentsBound())
    item.defaultInstance.hoverName else Component.translatable(item.descriptionId)

internal fun itemName(rule: ProfileRule): Component = if (rule.type == null) screenComponent("any_item") else
    registryItem(rule.type)?.let { localizedItemName(it) } ?: Component.literal(rule.type!!)

/** Draws an item enlarged by an integer [scale], keeping its pixels sharp. */
internal fun drawItem(graphics: GuiGraphicsExtractor, stack: ItemStack, x: Int, y: Int, scale: Int = 1) {
    if (stack.isEmpty) return
    graphics.pose().pushMatrix()
    graphics.pose().translate(x.toFloat(), y.toFloat())
    graphics.pose().scale(scale.toFloat(), scale.toFloat())
    graphics.item(stack, 0, 0)
    graphics.pose().popMatrix()
}

/** Stack count and durability at normal text size, inside the slot's bottom-right corner and above any frame. */
internal fun drawDecorations(graphics: GuiGraphicsExtractor, stack: ItemStack, x: Int, y: Int, size: Int) {
    if (stack.isEmpty) return
    val inset = size / 18 + 1
    graphics.itemDecorations(client.font, stack, x + size - inset - 17, y + size - inset - 17)
}

internal fun drawSlot(graphics: GuiGraphicsExtractor, x: Int, y: Int, size: Int = 18) {
    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, x, y, size, size)
}

/** Draws glyphs such as `*` for "any item" where an item icon would be. */
internal fun drawGlyph(graphics: GuiGraphicsExtractor, glyph: String, x: Int, y: Int, size: Int, color: Int) {
    val scale = size / 18
    graphics.pose().pushMatrix()
    graphics.pose().translate((x + size / 2).toFloat(), (y + size / 2 - 4 * scale).toFloat())
    graphics.pose().scale(scale.toFloat(), scale.toFloat())
    graphics.centeredText(client.font, glyph, 0, 0, color)
    graphics.pose().popMatrix()
}

/** Tints the inside of a slot; draw it before the item so the item stays readable. */
internal fun tintSlot(graphics: GuiGraphicsExtractor, x: Int, y: Int, size: Int, color: Int) {
    graphics.fill(x + 1, y + 1, x + size - 1, y + size - 1, (color and 0x00FFFFFF) or 0x60000000)
}

/** Frames a slot with a border that grows with the slot, so it stays visible on large screens. */
internal fun frameSlot(graphics: GuiGraphicsExtractor, x: Int, y: Int, size: Int, color: Int) {
    val border = size / 18 + 1
    graphics.fill(x, y, x + size, y + border, color)
    graphics.fill(x, y + size - border, x + size, y + size, color)
    graphics.fill(x, y + border, x + border, y + size - border, color)
    graphics.fill(x + size - border, y + border, x + size, y + size - border, color)
}

internal fun drawLock(graphics: GuiGraphicsExtractor, x: Int, y: Int, size: Int) {
    if (size < 36) return
    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LOCK_SPRITE, x + size - 12, y + 3, 10, 14)
}

internal fun drawCheck(graphics: GuiGraphicsExtractor, x: Int, y: Int, size: Int) {
    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CHECK_SPRITE, x + size - 10, y + 1, 9, 8)
}

/** Item icon in a slot; `*` stands for any item and `?` for IDs this game does not know. */
internal fun drawRuleIcon(graphics: GuiGraphicsExtractor, type: String?, x: Int, y: Int) {
    drawSlot(graphics, x, y)
    val stack = itemStack(type)
    when {
        !stack.isEmpty -> graphics.item(stack, x + 1, y + 1)
        type == null -> drawGlyph(graphics, "*", x, y, 18, GOLD)
        else -> drawGlyph(graphics, "?", x, y, 18, MUTED)
    }
}

internal fun quantityLabel(rule: ProfileRule): Component = screenComponent("quantity.${rule.quantityMode.name.lowercase()}", rule.quantity)

internal fun ruleSummary(rule: ProfileRule, keep: Boolean): Component {
    if (keep) return screenComponent("rule.keep_all")
    val parts = mutableListOf(quantityLabel(rule))
    if (rule.minimumInventory > 0) parts += screenComponent("rule.minimum_summary", rule.minimumInventory)
    if (hasPredicate(rule)) parts += screenComponent("rule.predicate_summary")
    return parts.reduce { summary, part -> summary.copy().append(" · ").append(part) }
}

private fun hasPredicate(rule: ProfileRule) = rule.predicate.isNotBlank() && rule.predicate != "[]"

/** Everything a rule does, with the values highlighted; the row itself only has room for a summary. */
internal fun ruleTooltip(rule: ProfileRule, keep: Boolean): Component {
    fun value(value: Any) = Component.literal(value.toString()).withStyle(ChatFormatting.WHITE)
    val tip = itemName(rule).copy().withStyle(ChatFormatting.WHITE)
        .append(Component.literal("\n" + (rule.type ?: "*")).withStyle(ChatFormatting.DARK_GRAY))
    if (rule.type == null) tip.append("\n").append(screenComponent("any_item.description").withStyle(ChatFormatting.GOLD))
    else if (registryItem(rule.type) == null) tip.append("\n").append(screenComponent("tooltip.unknown").withStyle(ChatFormatting.RED))
    tip.append("\n")
    if (keep) tip.append("\n").append(screenComponent("rule.keep_all").withStyle(ChatFormatting.GRAY))
    else {
        tip.append("\n").append(screenComponent("rule.drop_amount", quantityLabel(rule).copy().withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GRAY))
        if (rule.minimumInventory > 0)
            tip.append("\n").append(screenComponent("tooltip.minimum", value(rule.minimumInventory)).withStyle(ChatFormatting.GRAY))
    }
    if (hasPredicate(rule)) tip.append("\n").append(screenComponent("tooltip.predicate").withStyle(ChatFormatting.GRAY)).append("\n").append(value(rule.predicate))
    return tip.append("\n\n").append(screenComponent("edit_rule.hint").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC))
}

internal fun reasonText(decision: SlotDecision): Component = screenComponent(
    "reason.${decision.reason.name.lowercase()}", decision.profiles.joinToString(", "), decision.total, decision.required
)

internal fun slotName(slot: Int): Component = when (slot) {
    in 5..8 -> screenComponent("slot.armor.${slot - 5}")
    in 36..44 -> screenComponent("slot.hotbar", slot - 35)
    45 -> screenComponent("slot.offhand")
    else -> screenComponent("slot.inventory", slot - 8)
}
