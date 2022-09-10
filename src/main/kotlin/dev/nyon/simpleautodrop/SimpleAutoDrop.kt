package dev.nyon.simpleautodrop

import com.mojang.blaze3d.platform.InputConstants
import dev.nyon.simpleautodrop.config.autoDropCommand
import dev.nyon.simpleautodrop.config.loadConfig
import dev.nyon.simpleautodrop.config.saveConfig
import dev.nyon.simpleautodrop.config.settings
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
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

    fun onTake(player: Player, inventory: Inventory) {
        if (!settings.enabled) return
        val itemIds = settings.items.map { Item.getId(it) }
        inventory.items.forEachIndexed { index, itemStack ->
            if (!itemIds.contains(Item.getId(itemStack.item))) return@forEachIndexed
            player.drop(itemStack, false, true)
            inventory.setItem(index, ItemStack.EMPTY)
        }
    }

}