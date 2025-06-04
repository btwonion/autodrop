package dev.nyon.autodrop.config.screen.modify

import dev.nyon.autodrop.extensions.screenComponent
import dev.nyon.autodrop.minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput

class DropEverythingWidget(x: Int, y: Int, width: Int, height: Int, var bool: Boolean, val onTick: Boolean.() -> Unit) : AbstractWidget(
    x, y, width, height, screenComponent("modify.drop.description")
) {
    override fun renderWidget(
        guiGraphics: GuiGraphics, i: Int, j: Int, f: Float
    ) {
        val hundredPercentAlphaWhite = 0xFFFFFFFF.toInt()
        val component = screenComponent("modify.drop.description")
        guiGraphics.drawString(minecraft.font, component, x, y + height / 4, hundredPercentAlphaWhite)

        // tick box - outer rectangle
        val rightX = x + width
        guiGraphics.hLine(rightX, rightX - height, y, hundredPercentAlphaWhite)
        guiGraphics.hLine(rightX, rightX - height, y + height - 1, hundredPercentAlphaWhite)
        guiGraphics.vLine(rightX, y, y + height - 1, hundredPercentAlphaWhite)
        guiGraphics.vLine(rightX - height, y, y + height - 1, hundredPercentAlphaWhite)

        // tick box - inner square
        if (bool) guiGraphics.fill(
            rightX - 1, y + 2, rightX - height + 2, y + height - 2, hundredPercentAlphaWhite
        )
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        bool = !bool
        onTick(bool)

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {}
}