package net.nyon.simpleautodrop

import net.minecraft.client.Minecraft
import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.nyon.simpleautodrop.config.configGui
import net.nyon.simpleautodrop.config.loadConfig
import net.nyon.simpleautodrop.config.settings
import org.lwjgl.glfw.GLFW

private val toggleKeyBind = KeyBindingHelper.registerKeyBinding(
    KeyMapping(
        "Toggle AutoDrop", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, "SimpleAutoDrop"
    )
)
private val configGUIKeyBind = KeyBindingHelper.registerKeyBinding(
    KeyMapping(
        "AutoDrop", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, "SimpleAutoDrop"
    )
)

fun init() {
    toggleKeyBind
    configGUIKeyBind

    loadConfig()
}

object SimpleAutoDropMixinEntrypoint {

    private var gui = false

    fun tick(client: Minecraft) {
        while (toggleKeyBind.consumeClick()) {
            !settings.enabled
        }
        while (configGUIKeyBind.consumeClick()) {
            gui = true
        }

        if (gui) {
            gui = false
            client.setScreen(configGui())
        }
    }

}