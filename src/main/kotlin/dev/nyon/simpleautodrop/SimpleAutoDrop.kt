package dev.nyon.simpleautodrop

import com.mojang.blaze3d.platform.InputConstants
import dev.nyon.simpleautodrop.config.*
import dev.nyon.simpleautodrop.screen.ConfigScreen
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.world.InteractionHand
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import org.lwjgl.glfw.GLFW

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

        loadConfig()
        reloadCachedIds()
    }

    fun tick(client: Minecraft) {
        while (toggleKeyBind.consumeClick()) {
            settings.enabled = !settings.enabled
            saveConfig()
            client.gui.setOverlayMessage(
                Component.literal("SimpleAutoDrop ${if (settings.enabled) "enabled" else "disabled"}").withStyle(
                    Style.EMPTY.withColor(0xF99147)
                ), false
            )
        }
        while (menuKeyBind.consumeClick()) {
            client.setScreen(ConfigScreen(null))
        }
    }

    fun onTake() {
        if (!settings.enabled) return
        if (settings.currentArchives.isEmpty()) return
        val minecraft = Minecraft.getInstance()
        val player = minecraft.player ?: return
        val screen = InventoryScreen(player)
        screen.menu.slots.forEachIndexed { i, slot ->
            if (slot.item.item == Items.AIR || !itemIds.contains(Item.getId(slot.item.item)) || !slot.hasItem()) return@forEachIndexed
            minecraft.gameMode?.handleInventoryMouseClick(
                screen.menu.containerId, i, 1, ClickType.THROW, player
            )
            player.swing(InteractionHand.MAIN_HAND)
        }
    }
}