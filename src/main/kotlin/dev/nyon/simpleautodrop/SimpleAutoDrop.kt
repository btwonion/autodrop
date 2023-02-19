package dev.nyon.simpleautodrop

import com.mojang.blaze3d.platform.InputConstants
import dev.nyon.simpleautodrop.config.*
import dev.nyon.simpleautodrop.screen.ConfigScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import kotlin.time.Duration.Companion.milliseconds

val scope = CoroutineScope(Dispatchers.Default)

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
        reloadArchiveProperties()
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

    private var jobWaiting = false
    fun onTake() {
        if (!settings.enabled) return
        if (settings.activeArchives.isEmpty()) return
        val minecraft = Minecraft.getInstance()
        val player = minecraft.player ?: return
        if (jobWaiting) return
        jobWaiting = true

        scope.launch {
            delay(200.milliseconds)

            val screen = InventoryScreen(player)
            screen.menu.slots.forEachIndexed { _, slot ->
                if (blockedSlots.contains(slot.index)) return@forEachIndexed
                if (slot.item.item == Items.AIR || !itemIds.contains(Item.getId(slot.item.item)) || !slot.hasItem()) return@forEachIndexed
                minecraft.gameMode?.handleInventoryMouseClick(
                    screen.menu.containerId, slot.index, 1, ClickType.THROW, player
                )
            }

            jobWaiting = false
        }
    }
}