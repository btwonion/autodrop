package dev.nyon.autodrop.config.screen

import dev.nyon.autodrop.KeyBindings
import dev.nyon.autodrop.config.ConfigDraft
import dev.nyon.autodrop.extensions.screenComponent
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractButton

class GeneralSettingsScreen(private val parent: ProfileWorkspaceScreen, draft: ConfigDraft) : DraftScreen(screenComponent("general"), draft) {
    private var enabled = draft.value.enabled
    private var pickup = draft.value.triggerConfig.onPickup
    private var sneak = draft.value.triggerConfig.onSneak
    private var delay = draft.value.dropDelay.toString()
    private var done: AbstractButton? = null
    private var scroll = 0.0
    private var list: RowList? = null
    private var area = 300

    override fun init() {
        area = minOf(width - 16, 340)
        list = addRenderableWidget(RowList((width - area) / 2, HEADER + 6, area, height - HEADER - FOOTER - 30)).also { list ->
            val w = list.rowWidth
            list.rows(listOf(
                OptionRow(rowButton(toggle("general.enabled", enabled)) { enabled = !enabled; rebuild() }, screenComponent("general.enabled.description"), w),
                HeadingRow(screenComponent("general.triggers")),
                OptionRow(rowButton(toggle("general.pickup", pickup)) { pickup = !pickup; rebuild() }, screenComponent("general.pickup.description"), w),
                OptionRow(rowButton(toggle("general.sneak", sneak)) { sneak = !sneak; rebuild() }, screenComponent("general.sneak.description"), w),
                TextRow(screenComponent("general.key", KeyBindings.triggerKeyBind.translatedKeyMessage), w),
                HeadingRow(screenComponent("general.timing")),
                FieldRow(screenComponent("general.delay"), delay, screenComponent("general.delay.description"), w, inline = true) {
                    delay = it; done?.active = validDelay()
                }
            ))
            list.setScrollAmount(scroll)
        }
        done = footerButtons(screenComponent("use_settings")) {
            if (!validDelay()) return@footerButtons
            draft.value.enabled = enabled; draft.value.dropDelay = delay.toLong()
            draft.value.triggerConfig.onPickup = pickup; draft.value.triggerConfig.onSneak = sneak
            show(parent)
        }.also { it.active = validDelay() }
    }
    private fun toggle(key: String, value: Boolean) = screenComponent("toggle", screenComponent(key), screenComponent(if (value) "enabled" else "disabled"))
    private fun validDelay() = delay.toLongOrNull() in 0..Int.MAX_VALUE.toLong()
    private fun rebuild() { scroll = list?.scrollAmount() ?: 0.0; rebuildKeepingFocus() }
    override fun onClose() { show(parent) }
    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        super.extractRenderState(graphics, mouseX, mouseY, delta)
        if (!validDelay()) line(graphics, screenComponent("invalid.delay"), (width - area) / 2, height - FOOTER - 13, area, GOLD, mouseX, mouseY, centered = true)
    }
}
