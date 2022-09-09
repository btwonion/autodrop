package dev.nyon.simpleautodrop

import com.mojang.blaze3d.platform.InputConstants
import dev.nyon.simpleautodrop.config.autoDropCommand
import dev.nyon.simpleautodrop.config.loadConfig
import dev.nyon.simpleautodrop.config.settings
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW


object SimpleAutoDrop {

    private val toggleKeyBind = KeyBindingHelper.registerKeyBinding(
        KeyMapping(
            "Toggle AutoDrop", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, "SimpleAutoDrop"
        )
    )

    fun init() {
        toggleKeyBind

        autoDropCommand

        loadConfig()
    }

    fun tick(client: Minecraft) {
        while (toggleKeyBind.consumeClick()) {
            !settings.enabled
        }
    }

}