package dev.nyon.simpleautodrop.screen

import com.mojang.blaze3d.vertex.PoseStack
import dev.nyon.simpleautodrop.config.settings
import net.minecraft.client.gui.screens.Screen
import net.silkmc.silk.core.text.literalText

class ConfigScreen(private val previousScreen: Screen?) : Screen(literalText("SimpleAutoDrop")) {

    var currentArchive: String = ""

    override fun init() {
        currentArchive = settings.currentArchive ?: settings.items.entries.first().key
        addRenderableWidget(
            ArchiveListWidget(
                this.minecraft ?: return,
                this.width - 40,
                20,
                this.height - 40,
                0,
                this.height,
                24,
                currentArchive
            )
        )
    }

    override fun render(poseStack: PoseStack, i: Int, j: Int, f: Float) {
        renderDirtBackground(1)
        super.render(poseStack, i, j, f)
    }

    override fun onClose() {
        minecraft?.setScreen(previousScreen)
    }
}