package dev.nyon.autodrop.config.screen

import dev.nyon.autodrop.AutoDrop
import dev.nyon.autodrop.config.*
import dev.nyon.autodrop.extensions.screenComponent
import dev.nyon.autodrop.planning.*
import dev.nyon.konfig.config.saveConfig
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class ProfileWorkspaceScreen(private val parent: Screen?, draft: ConfigDraft = ConfigDraft(config)) :
    DraftScreen(screenComponent("title"), draft) {
    private var selectedIndex = 0
    private val selected get() = draft.value.profiles.getOrNull(selectedIndex)

    private enum class Section {
        ITEMS,
        SLOTS,
        PREVIEW
    }

    private var section = Section.ITEMS
    private var profileList: RowList? = null
    private var contentList: RowList? = null
    private var profileScroll = 0.0
    private var contentScroll = 0.0
    private var status: Component? = null
    private var plan = DropPlan(emptyList())
    private var snapshot: PlayerInventorySnapshot? = null
    private var lastItems: Any? = null
    private var discard: Button? = null
    private var hotbarButton: Button? = null
    private var clearButton: Button? = null
    private var left = 8
    private var total = 300
    private var paneX = 116
    private var paneWidth = 196

    init {
        AutoDrop.setEditing(true)
    }

    override fun init() { // Keep the workspace compact on huge screens; the inventory grid scales up instead.
        total = minOf(width - 16, 680)
        left = (width - total) / 2
        val side = (total / 4).coerceIn(96, 160)
        paneX = left + side + 12
        paneWidth = total - side - 12
        val top = HEADER + 6
        val bottom = height - FOOTER - 18
        hotbarButton = null; clearButton = null
        refreshPlan()

        profileList = addRenderableWidget(RowList(left, top, side, bottom - top - 24)).also { list ->
            list.rows(draft.value.profiles.mapIndexed { index, profile ->
                ProfileRow(
                    profile,
                    index == selectedIndex,
                    { rememberScroll(); selectedIndex = index; contentScroll = 0.0; rebuild() },
                    { toggle(profile) })
            })
            list.setScrollAmount(profileScroll)
        }
        button(screenComponent("new_profile"), left, bottom - 20, side) {
            rememberScroll()
            draft.value.profiles += Profile(
                name = screenComponent(
                    "profile_default",
                    draft.value.profiles.size + 1
                ).string
            )
            selectedIndex = draft.value.profiles.lastIndex; section = Section.ITEMS; contentScroll = 0.0
            rebuild()
        }

        val profile = selected
        if (profile == null) {
            contentList = addRenderableWidget(RowList(paneX, top, paneWidth, bottom - top)).also {
                it.rows(listOf(TextRow(screenComponent("profiles.empty"), it.rowWidth, centered = true)))
            }
        } else profilePane(profile, top, bottom)

        button(screenComponent("general"), left, height - 26, minOf(150, (total - 208) / 2).coerceAtLeast(80)) {
            rememberScroll(); show(GeneralSettingsScreen(this, draft))
        }
        discard = button(screenComponent("discard"), left + total - 204, height - 26, 100) {
            draft.discard(); status = null; selectedIndex =
            selectedIndex.coerceAtMost(draft.value.profiles.lastIndex).coerceAtLeast(0); rebuild()
        }.also { it.active = draft.dirty }
        button(screenComponent("save"), left + total - 100, height - 26, 100) { save() }.setTooltip(
                Tooltip.create(
                    screenComponent("save.description")
                )
            )
    }

    private fun profilePane(profile: Profile, top: Int, bottom: Int) {
        val deleteWidth = font.width(screenComponent("delete_profile")) + 16
        val modeWidth = ProfileMode.entries.maxOf { font.width(modeLabel(it)) } + 16
        val nameWidth = paneWidth - modeWidth - deleteWidth - 8
        addRenderableWidget(EditBox(font, paneX, top, nameWidth, 20, screenComponent("profile_name"))).also { field ->
            field.setMaxLength(80); field.value = profile.name; field.moveCursorToStart(false)
            field.setHint(screenComponent("profile_name"))
            field.setResponder { profile.name = it; refreshProfileNames() }
        }
        button(modeLabel(profile.mode), paneX + nameWidth + 4, top, modeWidth) {
            profile.mode = if (profile.mode == ProfileMode.DROP) ProfileMode.KEEP else ProfileMode.DROP
            profile.keepReviewed = false
            if (profile.mode == ProfileMode.KEEP) profile.enabled = false
            rebuild()
        }.setTooltip(
            Tooltip.create(
                screenComponent("mode.${profile.mode.name.lowercase()}.description").append("\n\n")
                    .append(screenComponent("mode.switch"))
            )
        )
        addRenderableWidget(DangerButton(screenComponent("delete_profile")) {
            rememberScroll()
            show(
                ConfirmDraftScreen(
                    this,
                    draft,
                    screenComponent("delete.title"),
                    screenComponent("delete.description", profile.name),
                    screenComponent("delete_profile")
                ) {
                    draft.value.profiles.removeAt(draft.value.profiles.indexOfFirst { it === profile })
                    selectedIndex = selectedIndex.coerceAtMost(draft.value.profiles.lastIndex).coerceAtLeast(0)
                    contentScroll = 0.0; rebuild()
                })
        }).setRectangle(deleteWidth, 20, paneX + paneWidth - deleteWidth, top)

        val tabsY = top + 24
        val tabWidth = paneWidth / Section.entries.size
        Section.entries.forEachIndexed { index, tab ->
            addRenderableWidget(TabWidget(screenComponent("section.${tab.name.lowercase()}"), section == tab) {
                rememberScroll(); section = tab; contentScroll = 0.0; rebuild()
            }).setRectangle(
                if (index == Section.entries.lastIndex) paneWidth - index * tabWidth else tabWidth,
                TAB_HEIGHT,
                paneX + index * tabWidth,
                tabsY
            )
        }

        val listTop =
            tabsY + TAB_HEIGHT + 4 // Actions for the open tab sit at the bottom of the pane, below the list they act on.
        val listBottom = if (section == Section.PREVIEW) bottom else bottom - 24
        contentList = addRenderableWidget(RowList(paneX, listTop, paneWidth, listBottom - listTop)).also { list ->
            val rowWidth = list.rowWidth
            list.rows(
                when (section) {
                    Section.ITEMS -> itemRows(profile, rowWidth)
                    Section.SLOTS -> slotRows(profile, rowWidth, list.height)
                    Section.PREVIEW -> previewRows(rowWidth, list.height)
                }
            )
            list.setScrollAmount(contentScroll)
        }
        when (section) {
            Section.ITEMS -> button(screenComponent("add_items"), paneX, bottom - 20, paneWidth) {
                rememberScroll(); show(BulkItemPickerScreen(this, draft, profile))
            }
            Section.SLOTS -> {
                val half = (paneWidth - 4) / 2
                hotbarButton = button(Component.empty(), paneX, bottom - 20, half) {
                    if (profile.protectedSlots.containsAll(HOTBAR)) profile.protectedSlots.removeAll(HOTBAR) else profile.protectedSlots.addAll(
                        HOTBAR
                    )
                    refreshPlan(); updateSlotButtons(profile)
                }
                clearButton =
                    button(screenComponent("slots.clear"), paneX + half + 4, bottom - 20, paneWidth - half - 4) {
                        profile.protectedSlots.clear(); refreshPlan(); updateSlotButtons(profile)
                    }
                updateSlotButtons(profile)
            }
            Section.PREVIEW -> {}
        }
    }

    private fun itemRows(profile: Profile, rowWidth: Int): List<NativeRow> {
        if (profile.entries.isEmpty()) return listOf(TextRow(screenComponent("items.empty"), rowWidth, centered = true))
        return profile.entries.map { rule ->
            RuleRow(profile, rule, { rememberScroll(); show(RuleEditorScreen(this, draft, profile, rule)) }) {
                rememberScroll(); profile.entries.removeAt(profile.entries.indexOfFirst { it === rule }); keepListChanged(
                profile
            ); rebuild()
            }
        }
    }

    private fun slotRows(profile: Profile, rowWidth: Int, listHeight: Int): List<NativeRow> {
        val description = TextRow(screenComponent("slots.description"), rowWidth)
        val legend = LegendRow(listOf(GREEN to screenComponent("legend.protected")))
        val scale = inventoryScale(rowWidth, listHeight - description.rowHeight - legend.rowHeight - 8)
        return listOf(description, InventoryRow(profile, { snapshot }, { plan }, false, scale) { slot ->
            if (!profile.protectedSlots.add(slot)) profile.protectedSlots.remove(slot)
            refreshPlan(); updateSlotButtons(profile)
        }, legend)
    }

    private fun previewRows(rowWidth: Int, listHeight: Int): List<NativeRow> = buildList {
        add(TextRow(screenComponent("preview.description"), rowWidth))
        val current = snapshot
        if (current == null) {
            add(TextRow(screenComponent("no_inventory"), rowWidth, WHITE)); return@buildList
        }
        add(InventoryRow(selected, { snapshot }, { plan }, true, inventoryScale(rowWidth, listHeight - 60, 2)) {})
        add(LegendRow(listOf(RED to screenComponent("legend.drop"), GREEN to screenComponent("legend.kept"))))
        plan.slots.count { it.method == DropMethod.SINGLE_ITEMS }.takeIf { it > 0 }?.let {
            add(TextRow(screenComponent("preview.single_items.summary", it), rowWidth, GOLD))
        }
        if (plan.slots.isEmpty()) add(TextRow(screenComponent("preview.empty"), rowWidth, centered = true))
        else detailed(plan).takeIf { it.isNotEmpty() }?.let { details ->
            add(HeadingRow(screenComponent("preview.details")))
            details.forEach { add(PreviewDecisionRow(it, current.items.getValue(it.slot), rowWidth)) }
            add(TextRow(screenComponent("preview.overlap"), rowWidth))
        }
    }

    /** The grid reads the profile every frame, so slot changes only need these buttons refreshed and focus stays put. */
    private fun updateSlotButtons(profile: Profile) {
        hotbarButton?.message =
            screenComponent(if (profile.protectedSlots.containsAll(HOTBAR)) "slots.hotbar.off" else "slots.hotbar.on")
        clearButton?.active = profile.protectedSlots.isNotEmpty()
    }

    private fun refreshProfileNames() {
        profileList?.children()?.filterIsInstance<ProfileRow>()?.forEach { it.update() }
    }

    private fun toggle(profile: Profile) {
        rememberScroll()
        if (!profile.enabled && profile.mode == ProfileMode.KEEP) {
            if (profile.entries.isEmpty()) {
                status = screenComponent("invalid.empty_keep", profile.name); rebuild(); return
            }
            if (!profile.keepReviewed) {
                show(KeepReviewScreen(this, draft, profile)); return
            }
        }
        profile.enabled = !profile.enabled; status = null; rebuild()
    }

    private fun save() {
        val issue = validateConfig(draft.value, ::predicateError)
        if (issue != null) {
            status = screenComponent(issue.key, issue.detail); rebuild(); return
        }
        try {
            val saved = draft.value.deepCopy()
            saveConfig(saved)
            config = saved
            AutoDrop.cancelPending()
            AutoDrop.setEditing(false)
            show(parent)
        } catch (e: Exception) {
            status = screenComponent("save_error", e.message ?: ""); rebuild()
        }
    }

    private fun rememberScroll() {
        profileScroll = profileList?.scrollAmount() ?: profileScroll; contentScroll =
            contentList?.scrollAmount() ?: contentScroll
    }

    private fun rebuild() {
        refreshPlan(); rebuildKeepingFocus()
    }

    private fun refreshPlan() {
        snapshot = MinecraftPlanner.snapshot()
        plan = snapshot?.let { MinecraftPlanner.plan(draft.value, it) } ?: DropPlan(emptyList())
    }

    override fun tick() {
        refreshPlan()
        val items = snapshot?.items?.mapValues { (_, item) -> item.toString() to item.componentsPatch }
        if (lastItems != items) {
            lastItems = items
            if (section == Section.PREVIEW) {
                rememberScroll(); rebuild()
            }
        } // Name and slot edits can change whether Discard is available without rebuilding a focused field.
        discard?.active = draft.dirty
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        super.extractRenderState(graphics, mouseX, mouseY, delta)
        graphics.fill(paneX - 7, HEADER + 6, paneX - 6, height - FOOTER - 18, 0x30FFFFFF)
        val message = status ?: if (snapshot == null) screenComponent("status.no_world") else screenComponent(
            "preview.count",
            plan.dropCount,
            plan.stackCount
        )
        line(
            graphics,
            message,
            left,
            height - FOOTER - 13,
            total,
            if (status == null) MUTED else GOLD,
            mouseX,
            mouseY,
            centered = true
        )
    }

    override fun onClose() {
        rememberScroll()
        if (draft.dirty) show(
            ConfirmDraftScreen(
                this,
                draft,
                screenComponent("discard.title"),
                screenComponent("discard.description"),
                screenComponent("discard")
            ) {
                draft.discard(); AutoDrop.setEditing(false); show(parent)
            })
        else {
            AutoDrop.setEditing(false); show(parent)
        }
    }
}

private val HOTBAR = (36 .. 44).toSet()

/** Slots worth explaining in a preview, drops first; stacks no rule touches have nothing to explain. */
internal fun detailed(plan: DropPlan) =
    plan.slots.filter { it.reason != Reason.NO_RULE }.sortedByDescending { it.dropCount }

internal fun modeLabel(mode: ProfileMode) = screenComponent("mode.${mode.name.lowercase()}")
internal fun keepListChanged(profile: Profile) {
    if (profile.mode == ProfileMode.KEEP) {
        profile.enabled = false; profile.keepReviewed = false
    }
}

/** Sidebar entry: an on/off checkbox and the profile name, with its mode and number of items underneath. */
internal class ProfileRow(
    private val profile: Profile,
    private val selected: Boolean,
    select: () -> Unit,
    toggle: () -> Unit
) : NativeRow(28) {
    private val toggle = CheckboxButton(
        { profile.enabled },
        screenComponent("profile_toggle", profile.name),
        toggle
    ).also { controls += it }
    private val select = object : FlatButton(Component.literal(profile.name), select) {
        override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
            highlight(graphics)
            graphics.text(
                client.font,
                ellipsize(Component.literal(profile.name), width - 6),
                x + 4,
                y + 4,
                if (this@ProfileRow.selected) WHITE else 0xFFE0E0E0.toInt()
            )
            graphics.text(client.font, ellipsize(summary(), width - 6), x + 4, y + 14, MUTED)
        }
    }.also { controls += it }

    private fun summary() = screenComponent("profile.summary.${profile.mode.name.lowercase()}", profile.entries.size)
    fun update() {
        select.message = Component.literal(profile.name)
        select.setTooltip(Tooltip.create(Component.literal(profile.name).append("\n").append(summary())))
        val toggleLabel = screenComponent("profile_toggle", profile.name)
        toggle.message = toggleLabel
        toggle.setTooltip(Tooltip.create(toggleLabel))
    }

    init {
        update()
    }

    override fun extractContent(
        graphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        hovered: Boolean,
        delta: Float
    ) {
        if (selected) {
            graphics.fill(x, y + 1, x + width, y + 27, 0x30FFFFFF)
            graphics.outline(x, y + 1, width, 26, 0x80FFFFFF.toInt())
        }
        toggle.setRectangle(20, 20, x + 3, y + 4)
        select.setRectangle(width - 26, 24, x + 25, y + 2)
        renderControls(graphics, mouseX, mouseY, delta)
    }
}

/** An item rule: click the row to edit it; the cross removes it. */
internal class RuleRow(
    private val profile: Profile,
    private val rule: ProfileRule,
    edit: () -> Unit,
    remove: () -> Unit
) : NativeRow(26) {
    private val edit = object : FlatButton(itemName(rule), edit) {
        override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
            highlight(graphics)
            drawRuleIcon(graphics, rule.type, x + 2, y + 2)
            graphics.text(client.font, ellipsize(itemName(rule), width - 30), x + 26, y + 3, WHITE)
            graphics.text(
                client.font,
                ellipsize(ruleSummary(rule, profile.mode == ProfileMode.KEEP), width - 30),
                x + 26,
                y + 13,
                MUTED
            )
        }
    }.also {
        it.setTooltip(Tooltip.create(ruleTooltip(rule, profile.mode == ProfileMode.KEEP)))
        controls += it
    }
    private val remove = SpriteButton(
        sprite("widget/cross_button"),
        sprite("widget/cross_button_highlighted"),
        screenComponent("remove"),
        remove
    ).also { controls += it }

    override fun extractContent(
        graphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        hovered: Boolean,
        delta: Float
    ) {
        edit.setRectangle(width - 20, 24, x, y + 1)
        remove.setRectangle(14, 14, x + width - 15, y + 6)
        renderControls(graphics, mouseX, mouseY, delta)
    }
}
