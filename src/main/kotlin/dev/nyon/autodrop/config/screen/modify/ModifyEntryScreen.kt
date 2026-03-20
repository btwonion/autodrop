@file:OptIn(ExperimentalTime::class)

package dev.nyon.autodrop.config.screen.modify

import dev.nyon.autodrop.AutoDrop
import dev.nyon.autodrop.config.ArchiveEntry
import dev.nyon.autodrop.config.config
import dev.nyon.autodrop.config.screen.root.ArchiveScreen
import dev.nyon.autodrop.config.screen.root.INNER_PAD
import dev.nyon.autodrop.config.screen.root.OUTER_PAD
import dev.nyon.autodrop.extensions.*
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
        (itemEditBox.value.isBlank() || BuiltInRegistries.ITEM.getOptional(resourceLocation(itemEditBox.value)).isPresent) && amountEditBox.value.toIntOrNull()
            .let { it != null && it in 0 .. 64 }
    }

    private val lastIndex: Instant = Clock.System.now()
    private val itemEditBox: EditBox =
        EditBox(internalMinecraft.font, 0, 0, 20, 20, screenComponent("modify.empty")).also {
            it.select(10.0, 10.0)
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
            it.select(10.0, 10.0)
            it.setMaxLength(300)
            it.value = archiveEntry.predicate
            it.cursorPosition = 0
            it.setHighlightPos(0)
            it.setResponder { input ->
                val valid = input.isBlank() || kotlin.runCatching {
                    AutoDrop.itemPredicateArgument.parse(input.matchItemPredicate().stringReader())
                }.isSuccess
                it.setTextColor(if (valid) 0xFFFFFFFF.toInt() else 0xFFFF0000.toInt())
            }
        }

    private val amountEditBox: EditBox =
        EditBox(internalMinecraft.font, 0, 0, 20, 20, screenComponent("modify.empty")).also {
            it.select(10.0, 10.0)
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
            screenWidth / 4, OUTER_PAD + INNER_PAD + internalMinecraft.font.lineHeight
        )
        itemEditBox.width = screenWidth / 2

        componentsEditBox.setPosition(
            screenWidth / 4,
            OUTER_PAD * 2 + INNER_PAD * 3 + internalMinecraft.font.lineHeight * 2 + 20 + screenHeight / 6
        )
        componentsEditBox.width = screenWidth / 2
        if (internalMinecraft.connection == null) {
            guiGraphics.drawCenteredString(
                internalMinecraft.font,
                screenComponent("modify.components.enchantment_warning"),
                screenWidth / 2,
                OUTER_PAD * 2 + INNER_PAD * 3 + internalMinecraft.font.lineHeight * 2 + 40 + screenHeight / 6,
                0xFFFF0000.toInt()
            )
        }

        amountEditBox.setPosition(
            screenWidth / 4,
            OUTER_PAD * 3 + INNER_PAD * 4 + internalMinecraft.font.lineHeight * 3 + 20 * 2 + screenHeight / 6
        )
        amountEditBox.width = screenWidth / 2

        dropEverythingTickBox.setPosition(
            screenWidth / 4,
            OUTER_PAD * 4 + INNER_PAD * 6 + internalMinecraft.font.lineHeight * 4 + 20 * 2 + screenHeight / 6
        )
        dropEverythingTickBox.width = screenWidth / 2
        dropEverythingTickBox.height = internalMinecraft.font.lineHeight * 2

        // render done button
        doneButton.setPosition(
            screenWidth / 3, screenHeight - OUTER_PAD - doneButton.height
        )
        doneButton.width = screenWidth / 3
        doneButton.active = matcher()
        super.render(guiGraphics, mouseX, mouseY, tickDelta)

        // render description
        guiGraphics.drawCenteredString(
            internalMinecraft.font,
            screenComponent("modify.item.description"),
            screenWidth / 2,
            OUTER_PAD,
            0xFFFFFFFF.toInt()
        )

        // render components text and edit box
        guiGraphics.drawCenteredString(
            internalMinecraft.font,
            screenComponent("modify.components.description"),
            screenWidth / 2,
            OUTER_PAD * 2 + INNER_PAD * 2 + internalMinecraft.font.lineHeight + 20 + screenHeight / 6,
            0xFFFFFFFF.toInt()
        )

        // render amount text and edit box
        guiGraphics.drawCenteredString(
            internalMinecraft.font,
            screenComponent("modify.amount.description"),
            screenWidth / 2,
            OUTER_PAD * 3 + INNER_PAD * 3 + internalMinecraft.font.lineHeight * 2 + 20 * 2 + screenHeight / 6,
            0xFFFFFFFF.toInt()
        )
    }

    override fun afterMouseMove() {
        itemListWidget.mouseMoved(0.0, 0.0)
        super.afterMouseMove()
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