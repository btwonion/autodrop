package dev.nyon.simpleautodrop

import com.mojang.blaze3d.platform.InputConstants
import dev.nyon.simpleautodrop.config.*
import dev.nyon.simpleautodrop.screen.ConfigScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.world.InteractionHand
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.silkmc.silk.core.text.literalText
import org.lwjgl.glfw.GLFW

val modScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
object SimpleAutoDrop {

    private val toggleKeyBind = KeyBindingHelper.registerKeyBinding(
        KeyMapping(
            "Toggle AutoDrop", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, "SimpleAutoDrop"
        )
    )

    private val menuKeyBind = KeyBindingHelper.registerKeyBinding(
        KeyMapping(
            "Open GUI", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, "SimpleAutoDrop"
        )
    )

    fun init() {
        toggleKeyBind

        autoDropCommand

        loadConfig()
        settings.items.forEach { (key, value) ->
            itemIds[key] = value.map { Item.getId(it) }.toMutableList()
        }
    }

    fun tick(client: Minecraft) {
        while (toggleKeyBind.consumeClick()) {
            settings.enabled = !settings.enabled
            saveConfig()
            client.player?.sendSystemMessage(literalText("You ${if (settings.enabled) "enabled" else "disabled"} auto drop!") {
                color = 0x1A631F
            })
        }
        while (menuKeyBind.consumeClick()) {
            client.setScreen(ConfigScreen(null))
        }
    }

    fun onTake() {
        if (!settings.enabled) return
        if (settings.currentArchive == null) return
        val minecraft = Minecraft.getInstance()
        val player = minecraft.player ?: return
        val screen = InventoryScreen(player)
        screen.menu.slots.forEachIndexed { i, slot ->
            if (slot.item.item == Items.AIR || itemIds[settings.currentArchive]?.contains(Item.getId(slot.item.item)) == false || !slot.hasItem()) return@forEachIndexed
            minecraft.gameMode?.handleInventoryMouseClick(
                screen.menu.containerId, i, 1, ClickType.THROW, player
            )
            player.swing(InteractionHand.MAIN_HAND)
        }
    }
}