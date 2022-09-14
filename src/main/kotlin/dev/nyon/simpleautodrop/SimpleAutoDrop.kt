package dev.nyon.simpleautodrop

import com.mojang.blaze3d.platform.InputConstants
import dev.nyon.simpleautodrop.config.*
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
import net.minecraft.world.InteractionHand
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
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
        itemIds = settings.items.map { Item.getId(it) }.toMutableList()
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
        val player = minecraft.player ?: return
        val screen = InventoryScreen(player)
        player.inventory?.items?.forEachIndexed { index, itemStack ->
            if (itemStack.item == Items.AIR || !itemIds.contains(Item.getId(itemStack.item))) return@forEachIndexed
            if (index <= 8) {
                val action = ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS
                player.inventory.removeItem(itemStack)
                player.connection.send(ServerboundPlayerActionPacket(action, BlockPos.ZERO, Direction.DOWN))
            } else minecraft.gameMode?.handleInventoryMouseClick(
                screen.menu.containerId, index, 1, ClickType.THROW, player
            )
            player.swing(InteractionHand.MAIN_HAND)
        }
    }
}