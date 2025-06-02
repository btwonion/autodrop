package dev.nyon.autodrop.config.screen.ignored

import dev.nyon.autodrop.config.Archive
import dev.nyon.autodrop.config.config
import dev.nyon.autodrop.minecraft as internalMinecraft
import dev.nyon.autodrop.config.screen.root.INNER_PAD
import dev.nyon.autodrop.config.screen.root.OUTER_PAD
import dev.nyon.autodrop.extensions.resourceLocation
import dev.nyon.autodrop.extensions.screenComponent
import dev.nyon.konfig.config.saveConfig
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
/*? if <1.21.5 {*/
/*import com.mojang.blaze3d.systems.RenderSystem
//? if >=1.21.2
import net.minecraft.client.renderer.CoreShaders
import net.minecraft.client.renderer.GameRenderer
*//*?}*/
//? if <1.21.6 {
/*import net.minecraft.client.renderer.RenderType*/
//?} else {
import net.minecraft.client.renderer.RenderPipelines
//?}

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
            it.setFilter(matcher)
            it.onClick(10.0, 10.0)
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

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, tickDelta: Float) {
        ignoredSlotsEditBox.setPosition(
            internalMinecraft.screen!!.width / 4,
            OUTER_PAD + INNER_PAD + internalMinecraft.font.lineHeight
        )
        ignoredSlotsEditBox.width = internalMinecraft.screen!!.width / 2

        doneButton.setPosition(
            internalMinecraft.screen!!.width / 3,
            internalMinecraft.screen!!.height - OUTER_PAD - doneButton.height
        )
        doneButton.width = internalMinecraft.screen!!.width / 3
        doneButton.active = matcher(ignoredSlotsEditBox.value)

        super.render(guiGraphics, mouseX, mouseY, tickDelta)

        // render description
        guiGraphics.drawCenteredString(
            internalMinecraft.font,
            screenComponent("ignored.description"),
            internalMinecraft.screen!!.width / 2,
            OUTER_PAD,
            0xFFFFFFFF.toInt()
        )

        // render image
        val imageLocation = resourceLocation("autodrop:image/inventory-slots.png")
            ?: error("Failed to load inventory slot guide image.")
        val imageSize = height / 2

        /*? if <1.21.5 {*/
        /*RenderSystem.setShader(/^? if <1.21.2 {^//^GameRenderer::getPositionTexShader^//^?} else {^/CoreShaders.POSITION_TEX/^?}^/)

        RenderSystem.setShaderTexture(0, imageLocation)
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.enableDepthTest()
        *//*?}*/

        /*? if >=1.21.2 {*/
        guiGraphics.blit(
            /*? if <1.21.6 {*//* RenderType::guiTextured *//*?} else {*/ RenderPipelines.GUI_TEXTURED /*?}*/,
            imageLocation,
            internalMinecraft.screen!!.width / 2 - imageSize / 2,
            OUTER_PAD + INNER_PAD * 2 + internalMinecraft.font.lineHeight + 20,
            0F,
            0F,
            imageSize - 1,
            imageSize,
            imageSize,
            imageSize
        )
        /*?} else {*/
        /*guiGraphics.blit(
            imageLocation,
            internalMinecraft.screen!!.width / 2 - imageSize / 2,
            OUTER_PAD + INNER_PAD * 2 + internalMinecraft.font.lineHeight + 20,
            0,
            0F,
            0F,
            imageSize - 1,
            imageSize,
            imageSize,
            imageSize
        )
        *//*?}*/
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