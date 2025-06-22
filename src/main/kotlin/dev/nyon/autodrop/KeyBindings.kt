package dev.nyon.autodrop

import com.mojang.blaze3d.platform.InputConstants
import dev.nyon.autodrop.AutoDrop.invokeAutodrop
import dev.nyon.autodrop.config.config
import dev.nyon.autodrop.config.screen.root.ArchiveScreen
import dev.nyon.konfig.config.saveConfig
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import org.lwjgl.glfw.GLFW

object KeyBindings {
    private val toggleKeyBind by lazy {
        KeyMapping(
            "key.autodrop.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.autodrop.category"
        )
    }
    private val menuKeyBind by lazy {
        KeyMapping(
            "key.autodrop.gui", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, "key.autodrop.category"
        )
    }
    private val triggerKeyBind by lazy {
        KeyMapping(
            "key.autodrop.trigger", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.autodrop.category"
        )
    }
    val keyBinds: Map<KeyMapping, (Minecraft) -> Unit> = mapOf(
        toggleKeyBind to { client ->
            config.enabled = !config.enabled
            saveConfig(config)
            client.gui.setOverlayMessage(
                Component.translatable("menu.autodrop.name").append(" ").append(
                    Component.translatable(if (config.enabled) "menu.autodrop.overlay.enabled" else "menu.autodrop.overlay.disabled")
                ).withStyle(Style.EMPTY.withColor(0xF99147)), false
            )
            if (config.enabled) invokeAutodrop()
        },
        menuKeyBind to { client ->
            client.setScreen(ArchiveScreen(null))
        },
        triggerKeyBind to { invokeAutodrop() }
    )

    fun handleKeybindings(client: Minecraft) {
        keyBinds.forEach { bind, function -> if (bind.consumeClick()) function(client) }
    }
}