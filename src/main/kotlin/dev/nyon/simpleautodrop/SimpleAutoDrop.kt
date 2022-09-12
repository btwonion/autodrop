package dev.nyon.simpleautodrop

import com.mojang.blaze3d.platform.InputConstants
import dev.nyon.simpleautodrop.config.*
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.Item
import net.silkmc.silk.core.text.literalText
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
            settings.enabled = !settings.enabled
            saveConfig()
            client.player?.sendSystemMessage(literalText("You ${if (settings.enabled) "enabled" else "disabled"} auto drop!") {
                color = 0x1A631F
            })
        }
    }

    fun onTake() {
        if (!settings.enabled) return
        val minecraft = Minecraft.getInstance()
        val player = minecraft.player
        player?.inventory?.items?.forEachIndexed { index, itemStack ->
            if (!itemIds.contains(Item.getId(itemStack.item))) return@forEachIndexed
            player.inventoryMenu.clicked(index, 1, ClickType.THROW, player)
            println("ja")
        }
    }
}