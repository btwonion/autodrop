package dev.nyon.autodrop.config.screen.modify

import dev.nyon.autodrop.AutoDrop.minecraft
import dev.nyon.autodrop.extensions.screenComponent
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.input.MouseButtonEvent

class DropEverythingWidget(x: Int, y: Int, width: Int, height: Int, var bool: Boolean, val onTick: Boolean.() -> Unit) :
    AbstractWidget(
        x, y, width, height, screenComponent("modify.drop.description")
    ) {
    override fun extractWidgetRenderState(
        guiGraphics: GuiGraphicsExtractor, i: Int, j: Int, f: Float
    ) {
        val hundredPercentAlphaWhite = 0xFFFFFFFF.toInt()
        val component = screenComponent("modify.drop.description")
        guiGraphics.text(minecraft.font, component, x, y + height / 4, hundredPercentAlphaWhite)

        // tick box - outer rectangle
        val rightX = x + width
        guiGraphics.horizontalLine(rightX, rightX - height, y, hundredPercentAlphaWhite)
        guiGraphics.horizontalLine(rightX, rightX - height, y + height - 1, hundredPercentAlphaWhite)
        guiGraphics.verticalLine(rightX, y, y + height - 1, hundredPercentAlphaWhite)
        guiGraphics.verticalLine(rightX - height, y, y + height - 1, hundredPercentAlphaWhite)

        // tick box - inner square
        if (bool) guiGraphics.fill(
            rightX - 1, y + 2, rightX - height + 2, y + height - 2, hundredPercentAlphaWhite
        )
    }

    override fun mouseClicked(mouseButtonEvent: MouseButtonEvent, bl: Boolean): Boolean {
        bool = !bool
        onTick(bool)

        return super.mouseClicked(mouseButtonEvent, bl)
    }

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {}
}