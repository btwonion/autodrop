package dev.nyon.autodrop.config.screen.ignored

import dev.nyon.autodrop.config.Archive
import dev.nyon.autodrop.config.config
import dev.nyon.autodrop.config.screen.root.INNER_PAD
import dev.nyon.autodrop.config.screen.root.OUTER_PAD
import dev.nyon.autodrop.extensions.*
import dev.nyon.konfig.config.saveConfig
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.RenderPipelines
import dev.nyon.autodrop.AutoDrop.minecraft as internalMinecraft

class IgnoredSlotsScreen(private val archive: Archive, private val parent: Screen?) :
    Screen(screenComponent("ignored.title")) {
    private val matcher: (String) -> Boolean = { input ->
        val list = input.split(',').toMutableList().also { list ->
            list.removeIf { s -> s.isEmpty() }
        }
        list.all { s ->
            s.toIntOrNull() in 0 .. 99
        }
    }

    private val ignoredSlotsEditBox =
        EditBox(internalMinecraft.font, 0, 0, 20, 20, screenComponent("ignored.empty")).also {
            it.setMaxLength(500)
            it.value = archive.ignoredSlots.joinToString(separator = ",") { input -> input.toString() }
            it.setResponder { input ->
                val valid = matcher(input)
                it.setTextColor(if (valid) 0xFFFFFFFF.toInt() else 0xFFFF0000.toInt())
            }
            it.select(10.0, 10.0)
            it.cursorPosition = 0
            it.setHighlightPos(0)
        }

    private val doneButton = Button.builder(screenComponent("done")) {
        onClose()
    }.build()

    override fun init() {
        addRenderableWidget(ignoredSlotsEditBox)
        addRenderableWidget(doneButton)
        super.init()
    }

    override fun extractRenderState(guiGraphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, tickDelta: Float) {
        ignoredSlotsEditBox.setPosition(
            screenWidth / 4, OUTER_PAD + INNER_PAD + internalMinecraft.font.lineHeight
        )
        ignoredSlotsEditBox.width = screenWidth / 2

        doneButton.setPosition(
            screenWidth / 3, screenHeight - OUTER_PAD - doneButton.height
        )
        doneButton.width = screenWidth / 3
        doneButton.active = matcher(ignoredSlotsEditBox.value)

        super.extractRenderState(guiGraphics, mouseX, mouseY, tickDelta)

        // render description
        guiGraphics.centeredText(
            internalMinecraft.font,
            screenComponent("ignored.description"),
            screenWidth / 2,
            OUTER_PAD,
            0xFFFFFFFF.toInt()
        )

        // render image
        val imageLocation =
            identifier("autodrop:image/inventory-slots.png") ?: error("Failed to load inventory slot guide image.")
        val imageSize = height / 2

        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED,
            imageLocation,
            screenWidth / 2 - imageSize / 2,
            OUTER_PAD + INNER_PAD * 2 + internalMinecraft.font.lineHeight + 20,
            0F,
            0F,
            imageSize - 1,
            imageSize,
            imageSize,
            imageSize
        )
    }

    override fun shouldCloseOnEsc(): Boolean {
        return matcher(ignoredSlotsEditBox.value)
    }

    override fun onClose() {
        internalMinecraft.setScreen(parent)
        archive.ignoredSlots = ignoredSlotsEditBox.value.split(',').mapNotNull { it.toIntOrNull() }.toMutableSet()
        saveConfig(config)
    }
}