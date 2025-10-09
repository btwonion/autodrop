package dev.nyon.autodrop

import dev.nyon.autodrop.AutoDrop.invokeAutodrop
import dev.nyon.autodrop.config.config
import dev.nyon.autodrop.config.screen.root.ArchiveScreen
import dev.nyon.autodrop.extensions.keyMapping
import dev.nyon.konfig.config.saveConfig
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import org.lwjgl.glfw.GLFW

object KeyBindings {
    private val toggleKeyBind by lazy {
        keyMapping("key.autodrop.toggle", GLFW.GLFW_KEY_J)
    }
    private val menuKeyBind by lazy {
        keyMapping("key.autodrop.gui", GLFW.GLFW_KEY_O)
    }
    private val triggerKeyBind by lazy {
        keyMapping("key.autodrop.trigger", GLFW.GLFW_KEY_K)
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
        keyBinds.forEach { (bind, function) -> if (bind.consumeClick()) function(client) }
    }
}