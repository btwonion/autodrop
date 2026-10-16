package dev.nyon.autodrop.config.screen

import dev.nyon.autodrop.config.*
import dev.nyon.autodrop.extensions.screenComponent
import dev.nyon.autodrop.planning.*
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component

internal class ConfirmDraftScreen(
    private val parent: DraftScreen,
    draft: ConfigDraft,
    title: Component,
    private val description: Component,
    private val confirm: Component,
    private val accept: () -> Unit
) : DraftScreen(title, draft) {
    override fun init() {
        footerButtons(confirm, danger = true) { show(parent); accept() }
    }
    override fun onClose() { show(parent) }
    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        super.extractRenderState(graphics, mouseX, mouseY, delta)
        paragraphs(graphics, listOf(description to WHITE))
    }
}

internal class KeepReviewScreen(private val parent: ProfileWorkspaceScreen, draft: ConfigDraft, private val profile: Profile) :
    DraftScreen(screenComponent("keep_review.title"), draft) {
    private var offline = emptyList<Pair<Component, Int>>()
    override fun init() {
        val area = minOf(width - 16, 520)
        val proposed = draft.value.deepCopy()
        proposed.profiles[draft.value.profiles.indexOfFirst { it === profile }].enabled = true
        val masterOff = !proposed.enabled
        proposed.enabled = true
        val snapshot = MinecraftPlanner.snapshot()
        val explanation = buildList {
            add(screenComponent("keep_review.description", profile.name) to GOLD)
            add(screenComponent("keep_review.details") to MUTED)
            if (masterOff) add(screenComponent("keep_review.master_off") to MUTED)
        }
        // Without a world there is nothing to preview, so the dialog only explains what turning it on does.
        offline = if (snapshot == null) explanation + (screenComponent("keep_review.no_world") to WHITE) else emptyList()
        if (snapshot != null) {
            val plan = MinecraftPlanner.plan(proposed, snapshot)
            val list = addRenderableWidget(RowList((width - area) / 2, HEADER + 6, area, height - HEADER - FOOTER - 12))
            val w = list.rowWidth
            list.rows(explanation.map<Pair<Component, Int>, NativeRow> { (text, color) -> TextRow(text, w, color) } + buildList {
                add(TextRow(screenComponent("keep_review.preview", plan.dropCount, plan.stackCount), w, WHITE))
                add(InventoryRow(profile, { snapshot }, { plan }, true, inventoryScale(w, list.height - 80, 2)) {})
                add(LegendRow(listOf(RED to screenComponent("legend.drop"), GREEN to screenComponent("legend.kept"))))
                detailed(plan).forEach { add(PreviewDecisionRow(it, snapshot.items.getValue(it.slot), w)) }
            })
        }
        footerButtons(screenComponent("keep_review.enable")) {
            profile.keepReviewed = true; profile.enabled = true; show(parent)
        }.active = profile.entries.isNotEmpty() && profile.entries.all { predicateError(it.predicate) == null }
    }
    override fun onClose() { show(parent) }
    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        super.extractRenderState(graphics, mouseX, mouseY, delta)
        if (offline.isNotEmpty()) paragraphs(graphics, offline)
    }
}
