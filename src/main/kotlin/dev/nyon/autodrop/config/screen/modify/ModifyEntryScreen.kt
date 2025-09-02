@file:OptIn(ExperimentalTime::class)

package dev.nyon.autodrop.config.screen.modify

import dev.nyon.autodrop.AutoDrop
import dev.nyon.autodrop.config.ArchiveEntry
import dev.nyon.autodrop.config.config
import dev.nyon.autodrop.config.screen.root.ArchiveScreen
import dev.nyon.autodrop.config.screen.root.INNER_PAD
import dev.nyon.autodrop.config.screen.root.OUTER_PAD
import dev.nyon.autodrop.extensions.matchItemPredicate
import dev.nyon.autodrop.extensions.resourceLocation
import dev.nyon.autodrop.extensions.screenComponent
import dev.nyon.autodrop.extensions.stringReader
import dev.nyon.konfig.config.saveConfig
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.registries.BuiltInRegistries
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import dev.nyon.autodrop.AutoDrop.minecraft as internalMinecraft

class ModifyEntryScreen(private val parent: ArchiveScreen, private val archiveEntry: ArchiveEntry) :
    Screen(screenComponent("modify.title")) {
    private val matcher: () -> Boolean = {
        (itemEditBox.value.isBlank() || BuiltInRegistries.ITEM.getOptional(resourceLocation(itemEditBox.value)).isPresent) && (componentsEditBox.value.isBlank() || kotlin.runCatching {
            AutoDrop.itemPredicateArgument.parse(componentsEditBox.value.matchItemPredicate().stringReader())
        }.onFailure { println(it) }.isSuccess) && amountEditBox.value.toIntOrNull().let { it != null && it in 0 .. 64 }
    }

    private val lastIndex: Instant = Clock.System.now()
    private val itemEditBox: EditBox =
        EditBox(internalMinecraft.font, 0, 0, 20, 20, screenComponent("modify.empty")).also {
            it.onClick(10.0, 10.0)
            it.setMaxLength(100)
            it.setResponder { input ->
                val now = Clock.System.now()
                if (now - lastIndex > 500.milliseconds) {
                    itemListWidget.input = input
                    itemListWidget.refreshEntries()
                }
            }
            it.value = archiveEntry.type?.let { item -> BuiltInRegistries.ITEM.getKey(item).toString() } ?: ""
            it.cursorPosition = 0
            it.setHighlightPos(0)
        }

    private val componentsEditBox: EditBox =
        EditBox(internalMinecraft.font, 0, 0, 20, 20, screenComponent("modify.empty")).also {
            it.onClick(10.0, 10.0)
            it.setMaxLength(300)
            it.value = archiveEntry.predicate
            it.cursorPosition = 0
            it.setHighlightPos(0)
        }

    private val amountEditBox: EditBox =
        EditBox(internalMinecraft.font, 0, 0, 20, 20, screenComponent("modify.empty")).also {
            it.onClick(10.0, 10.0)
            it.setMaxLength(2)
            it.value = archiveEntry.amount.toString()
            it.setFilter { input ->
                if (input.isEmpty()) return@setFilter true
                val int = input.toIntOrNull() ?: return@setFilter false
                int in 0 .. 64
            }
            it.cursorPosition = 0
            it.setHighlightPos(0)
        }

    private val itemListWidget: ModifyItemsWidget = ModifyItemsWidget(itemEditBox.value) item@{
        archiveEntry.type = this@item
        itemEditBox.value = BuiltInRegistries.ITEM.getKey(this@item).toString()
    }.also {
        it.refreshEntries()
    }

    private val dropEverythingTickBox: DropEverythingWidget =
        DropEverythingWidget(0, 0, 20, 20, archiveEntry.dropEverything) {
            archiveEntry.dropEverything = this@DropEverythingWidget
        }

    private val doneButton = Button.builder(screenComponent("done")) {
        onClose()
    }.build()

    override fun init() {
        addRenderableWidget(itemEditBox)
        addRenderableWidget(componentsEditBox)
        addRenderableWidget(amountEditBox)
        addRenderableWidget(itemListWidget)
        addRenderableWidget(dropEverythingTickBox)
        addRenderableWidget(doneButton)
        super.init()
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, tickDelta: Float) { // render item edit box
        itemEditBox.setPosition(
            internalMinecraft.screen!!.width / 4, OUTER_PAD + INNER_PAD + internalMinecraft.font.lineHeight
        )
        itemEditBox.width = internalMinecraft.screen!!.width / 2

        componentsEditBox.setPosition(
            internalMinecraft.screen!!.width / 4,
            OUTER_PAD * 2 + INNER_PAD * 3 + internalMinecraft.font.lineHeight * 2 + 20 + internalMinecraft.screen!!.height / 6
        )
        componentsEditBox.width = internalMinecraft.screen!!.width / 2

        amountEditBox.setPosition(
            internalMinecraft.screen!!.width / 4,
            OUTER_PAD * 3 + INNER_PAD * 4 + internalMinecraft.font.lineHeight * 3 + 20 * 2 + internalMinecraft.screen!!.height / 6
        )
        amountEditBox.width = internalMinecraft.screen!!.width / 2

        dropEverythingTickBox.setPosition(
            internalMinecraft.screen!!.width / 4,
            OUTER_PAD * 4 + INNER_PAD * 6 + internalMinecraft.font.lineHeight * 4 + 20 * 2 + internalMinecraft.screen!!.height / 6
        )
        dropEverythingTickBox.width = internalMinecraft.screen!!.width / 2
        dropEverythingTickBox.height = internalMinecraft.font.lineHeight * 2

        // render done button
        doneButton.setPosition(
            internalMinecraft.screen!!.width / 3, internalMinecraft.screen!!.height - OUTER_PAD - doneButton.height
        )
        doneButton.width = internalMinecraft.screen!!.width / 3
        doneButton.active = matcher()
        super.render(guiGraphics, mouseX, mouseY, tickDelta)

        // render description
        guiGraphics.drawCenteredString(
            internalMinecraft.font,
            screenComponent("modify.item.description"),
            internalMinecraft.screen!!.width / 2,
            OUTER_PAD,
            0xFFFFFFFF.toInt()
        )

        // render components text and edit box
        guiGraphics.drawCenteredString(
            internalMinecraft.font,
            screenComponent("modify.components.description"),
            internalMinecraft.screen!!.width / 2,
            OUTER_PAD * 2 + INNER_PAD * 2 + internalMinecraft.font.lineHeight + 20 + internalMinecraft.screen!!.height / 6,
            0xFFFFFFFF.toInt()
        )

        // render amount text and edit box
        guiGraphics.drawCenteredString(
            internalMinecraft.font,
            screenComponent("modify.amount.description"),
            internalMinecraft.screen!!.width / 2,
            OUTER_PAD * 3 + INNER_PAD * 3 + internalMinecraft.font.lineHeight * 2 + 20 * 2 + internalMinecraft.screen!!.height / 6,
            0xFFFFFFFF.toInt()
        )
    }

    override fun shouldCloseOnEsc(): Boolean {
        return matcher()
    }

    override fun onClose() {
        archiveEntry.predicate = componentsEditBox.value.let { it.ifBlank { "[]" } }
        archiveEntry.amount = amountEditBox.value.toIntOrNull() ?: 1
        internalMinecraft.setScreen(parent)
        saveConfig(config)
        parent.archiveItemsWidget.refreshEntries()
    }
}