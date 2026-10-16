package dev.nyon.autodrop.config.screen

import dev.nyon.autodrop.config.*
import dev.nyon.autodrop.extensions.identifier
import dev.nyon.autodrop.extensions.screenComponent
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractButton
import net.minecraft.network.chat.Component

class RuleEditorScreen(private val parent: ProfileWorkspaceScreen, draft: ConfigDraft, private val profile: Profile, private val target: ProfileRule) :
    DraftScreen(screenComponent("edit_rule"), draft) {
    private val rule = target.copy()
    private var quantity = rule.quantity.toString()
    private var minimum = rule.minimumInventory.toString()
    private var type = rule.type ?: "*"
    private var advanced = false
    private var scroll = 0.0
    private var list: RowList? = null
    private var done: AbstractButton? = null
    private var error: Component? = null
    private var area = 300

    override fun init() {
        area = minOf(width - 16, 420)
        list = addRenderableWidget(RowList((width - area) / 2, HEADER + 6, area, height - HEADER - FOOTER - 30)).also { list ->
            val w = list.rowWidth
            val rows = mutableListOf<NativeRow>(ItemHeaderRow(rule))
            if (profile.mode == ProfileMode.DROP) {
                rows += HeadingRow(screenComponent("rule.amount"))
                rows += OptionRow(rowButton(screenComponent("rule.drop_amount", screenComponent("quantity.${rule.quantityMode.name.lowercase()}.mode"))) {
                    rule.quantityMode = QuantityMode.entries[(rule.quantityMode.ordinal + 1) % QuantityMode.entries.size]; rebuild()
                }, screenComponent("quantity.${rule.quantityMode.name.lowercase()}.description"), w)
                if (rule.quantityMode == QuantityMode.EXACT) {
                    rows += FieldRow(screenComponent("rule.exact"), quantity, screenComponent("rule.exact.description"), w, inline = true) { quantity = it; validate() }
                    rows += TextRow(screenComponent("rule.exact.warning"), w, GOLD)
                }
                rows += FieldRow(screenComponent("rule.minimum"), minimum, screenComponent("rule.minimum.description"), w, inline = true) { minimum = it; validate() }
            } else rows += TextRow(screenComponent("rule.keep_all.description"), w)
            rows += ButtonRow(rowButton(screenComponent(if (advanced) "advanced.hide" else "advanced.show")) { advanced = !advanced; rebuild() })
            if (advanced) {
                rows += FieldRow(screenComponent("rule.type"), type, screenComponent("rule.type.description"), w, 160) { type = it; validate() }
                rows += FieldRow(screenComponent("rule.predicate"), rule.predicate, screenComponent("rule.predicate.description"), w, 8192) { rule.predicate = it; validate() }
                if (client.player == null) rows += TextRow(screenComponent("rule.predicate.offline"), w)
            }
            list.rows(rows); list.setScrollAmount(scroll)
        }
        done = footerButtons(screenComponent("use_rule")) {
            if (!validate()) return@footerButtons
            val changedMatch = target.type != rule.type || target.predicate != rule.predicate
            target.type = rule.type; target.predicate = rule.predicate; target.quantityMode = rule.quantityMode
            target.quantity = rule.quantity; target.minimumInventory = rule.minimumInventory
            if (changedMatch) keepListChanged(profile)
            show(parent)
        }
        validate()
    }
    private fun validate(): Boolean {
        val id = type.trim()
        // Short IDs such as "stone" are normalized; the result must pass the same check as saving.
        val normalized = if (id == "*") null else identifier(id)?.toString()
        val exact = quantity.toIntOrNull()?.takeIf { it > 0 }
        val filterError = predicateError(rule.predicate)
        error = when {
            id != "*" && normalized?.let(::isValidRuleType) != true -> screenComponent("invalid.type")
            // X is only shown for exact amounts; in other modes the last valid X is kept.
            rule.quantityMode == QuantityMode.EXACT && exact == null -> screenComponent("invalid.exact")
            minimum.toIntOrNull()?.let { it >= 0 } != true -> screenComponent("invalid.minimum")
            filterError != null -> screenComponent("invalid.predicate", filterError)
            else -> null
        }
        if (error == null) {
            rule.type = normalized
            exact?.let { rule.quantity = it }; rule.minimumInventory = minimum.toInt()
        }
        done?.active = error == null
        return error == null
    }
    private fun rebuild() { scroll = list?.scrollAmount() ?: 0.0; rebuildKeepingFocus() }
    override fun onClose() { show(parent) }
    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        super.extractRenderState(graphics, mouseX, mouseY, delta)
        error?.let { line(graphics, it, (width - area) / 2, height - FOOTER - 13, area, GOLD, mouseX, mouseY, centered = true) }
    }
}

/** The edited item: its icon, name and registry ID, updated as the ID is edited. */
private class ItemHeaderRow(private val rule: ProfileRule) : NativeRow(30) {
    override fun extractContent(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, hovered: Boolean, delta: Float) {
        drawRuleIcon(graphics, rule.type, x, y + 5)
        text(graphics, itemName(rule), x + 24, y + 5, width - 24)
        text(graphics, Component.literal(rule.type ?: "*"), x + 24, y + 16, width - 24, MUTED)
    }
}
