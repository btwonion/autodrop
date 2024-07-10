package dev.nyon.autodrop.config.screen.ignored

import com.mojang.blaze3d.systems.RenderSystem
import dev.nyon.autodrop.config.Archive
import dev.nyon.autodrop.config.config
import dev.nyon.autodrop.extensions.resourceLocation
import dev.nyon.autodrop.extensions.screenComponent
import dev.nyon.konfig.config.saveConfig
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.GameRenderer
import dev.nyon.autodrop.minecraft as internalMinecraft

private const val IMAGE_SIZE = 254

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
            addWidget(it)
            it.value = archive.ignoredSlots.joinToString(separator = ",") { input -> input.toString() }
            it.setMaxLength(136)
            it.setFilter(matcher)
            it.onClick(10.0, 10.0)
            it.cursorPosition = 0
            it.setHighlightPos(0)
        }

    private val doneButton = Button.builder(screenComponent("done")) {
        onClose()
    }.build().also { addWidget(it) }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, tickDelta: Float) {
        renderBackground(guiGraphics, mouseX, mouseY, tickDelta)

        // render description
        guiGraphics.drawCenteredString(
            internalMinecraft.font,
            screenComponent("ignored.description"),
            internalMinecraft.screen!!.width / 2,
            internalMinecraft.screen!!.height / 6,
            0xFFFFFF
        )

        // render edit box
        ignoredSlotsEditBox.setPosition(internalMinecraft.screen!!.width / 3, internalMinecraft.screen!!.height / 4)
        ignoredSlotsEditBox.width = internalMinecraft.screen!!.width / 3
        ignoredSlotsEditBox.render(guiGraphics, mouseX, mouseY, tickDelta)

        // render image
        val imageLocation = resourceLocation("autodrop:image/inventory-slots.png")
        RenderSystem.setShader(GameRenderer::getPositionTexShader)
        RenderSystem.setShaderTexture(0, imageLocation)
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.enableDepthTest()
        guiGraphics.blit(
            imageLocation,
            internalMinecraft.screen!!.width / 2 - IMAGE_SIZE / 2,
            (internalMinecraft.screen!!.height * .55).toInt() - IMAGE_SIZE / 2,
            0,
            0,
            IMAGE_SIZE,
            IMAGE_SIZE
        )
        RenderSystem.disableBlend()
        RenderSystem.disableDepthTest()

        // render done button
        doneButton.setPosition(internalMinecraft.screen!!.width / 3, (internalMinecraft.screen!!.height * .8).toInt())
        doneButton.width = internalMinecraft.screen!!.width / 3
        doneButton.render(guiGraphics, mouseX, mouseY, tickDelta)
        doneButton.active = matcher(ignoredSlotsEditBox.value)
    }

    override fun shouldCloseOnEsc(): Boolean {
        return matcher(ignoredSlotsEditBox.value)
    }

    override fun onClose() {
        internalMinecraft.setScreen(parent)
        archive.ignoredSlots = ignoredSlotsEditBox.value.split(',').mapNotNull { it.toIntOrNull() }.toMutableList()
        saveConfig(config)
    }
}