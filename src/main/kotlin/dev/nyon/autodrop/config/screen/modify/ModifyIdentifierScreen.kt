package dev.nyon.autodrop.config.screen.modify

import dev.nyon.autodrop.config.ItemIdentifier
import dev.nyon.autodrop.config.config
import dev.nyon.autodrop.config.screen.root.ArchiveScreen
import dev.nyon.autodrop.extensions.DataComponentPatchSerializer
import dev.nyon.autodrop.extensions.resourceLocation
import dev.nyon.autodrop.extensions.screenComponent
import dev.nyon.konfig.config.saveConfig
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.registries.BuiltInRegistries
import kotlin.time.Duration.Companion.milliseconds
import dev.nyon.autodrop.minecraft as internalMinecraft

class ModifyIdentifierScreen(private val parent: ArchiveScreen, private val itemIdentifier: ItemIdentifier) :
    Screen(screenComponent("modify.title")) {
    private val matcher: () -> Boolean = {
        (itemEditBox.value.isBlank() || BuiltInRegistries.ITEM.getOptional(resourceLocation(itemEditBox.value)).isPresent) && kotlin.runCatching {
            DataComponentPatchSerializer.toPatch(componentsEditBox.value)
        }.isSuccess && amountEditBox.value.toIntOrNull().let { it != null && it in 0 .. 64 }
    }

    private val lastIndex: Instant = Clock.System.now()
    private val itemEditBox: EditBox =
        EditBox(internalMinecraft.font, 0, 0, 20, 20, screenComponent("modify.empty")).also {
            addWidget(it)
            it.onClick(10.0, 10.0)
            it.setResponder { input ->
                val now = Clock.System.now()
                if (now - lastIndex > 500.milliseconds) {
                    itemListWidget.input = input
                    itemListWidget.refreshEntries()
                }
            }
            it.value = itemIdentifier.type?.let { item -> BuiltInRegistries.ITEM.getKey(item).toString() } ?: ""
            it.cursorPosition = 0
            it.setHighlightPos(0)
        }

    private val componentsEditBox: EditBox =
        EditBox(internalMinecraft.font, 0, 0, 20, 20, screenComponent("modify.empty")).also {
            addWidget(it)
            it.onClick(10.0, 10.0)
            it.setMaxLength(300)
            it.value = DataComponentPatchSerializer.toString(itemIdentifier.components)
            it.cursorPosition = 0
            it.setHighlightPos(0)
        }

    private val amountEditBox: EditBox =
        EditBox(internalMinecraft.font, 0, 0, 20, 20, screenComponent("modify.empty")).also {
            addWidget(it)
            it.onClick(10.0, 10.0)
            it.setMaxLength(2)
            it.value = itemIdentifier.amount.toString()
            it.setFilter { input ->
                val int = input.toIntOrNull() ?: return@setFilter false
                int in 0 .. 64
            }
            it.cursorPosition = 0
            it.setHighlightPos(0)
        }

    private val itemListWidget: ModifyItemsWidget = ModifyItemsWidget(itemEditBox.value) item@{
        itemIdentifier.type = this@item
        itemEditBox.value = BuiltInRegistries.ITEM.getKey(this@item).toString()
    }.also {
        addWidget(it)
        it.refreshEntries()
    }

    private val doneButton = Button.builder(screenComponent("done")) {
        onClose()
    }.build().also { addWidget(it) }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, tickDelta: Float) {
        renderBackground(guiGraphics, mouseX, mouseY, tickDelta)

        // render description
        guiGraphics.drawCenteredString(
            internalMinecraft.font,
            screenComponent("modify.item.description"),
            internalMinecraft.screen!!.width / 2,
            internalMinecraft.screen!!.height / 10,
            0xFFFFFF
        )

        // render item edit box
        itemEditBox.setPosition(internalMinecraft.screen!!.width / 3, internalMinecraft.screen!!.height / 8)
        itemEditBox.width = internalMinecraft.screen!!.width / 3
        itemEditBox.render(guiGraphics, mouseX, mouseY, tickDelta)

        // render item list if edit box is selected
        itemListWidget.render(guiGraphics, mouseX, mouseY, tickDelta)

        // render components text and edit box
        guiGraphics.drawCenteredString(
            internalMinecraft.font,
            screenComponent("modify.components.description"),
            internalMinecraft.screen!!.width / 2,
            (internalMinecraft.screen!!.height * .4).toInt(),
            0xFFFFFF
        )

        componentsEditBox.setPosition(
            internalMinecraft.screen!!.width / 3, (internalMinecraft.screen!!.height * .45).toInt()
        )
        componentsEditBox.width = internalMinecraft.screen!!.width / 3
        componentsEditBox.render(guiGraphics, mouseX, mouseY, tickDelta)

        // render amount text and edit box
        guiGraphics.drawCenteredString(
            internalMinecraft.font,
            screenComponent("modify.amount.description"),
            internalMinecraft.screen!!.width / 2,
            (internalMinecraft.screen!!.height * .575).toInt(),
            0xFFFFFF
        )

        amountEditBox.setPosition(
            internalMinecraft.screen!!.width / 3, (internalMinecraft.screen!!.height * .625).toInt()
        )
        amountEditBox.width = internalMinecraft.screen!!.width / 3
        amountEditBox.render(guiGraphics, mouseX, mouseY, tickDelta)

        // render done button
        doneButton.setPosition(internalMinecraft.screen!!.width / 3, (internalMinecraft.screen!!.height * .8).toInt())
        doneButton.width = internalMinecraft.screen!!.width / 3
        doneButton.render(guiGraphics, mouseX, mouseY, tickDelta)
        doneButton.active = matcher()
    }

    override fun shouldCloseOnEsc(): Boolean {
        return matcher()
    }

    override fun onClose() {
        itemIdentifier.components = DataComponentPatchSerializer.toPatch(componentsEditBox.value)
        itemIdentifier.amount = amountEditBox.value.toInt()
        internalMinecraft.setScreen(parent)
        saveConfig(config)
        parent.archiveItemsWidget.refreshEntries()
    }
}