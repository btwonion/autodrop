package dev.nyon.simpleautodrop

import com.mojang.blaze3d.platform.InputConstants
import dev.nyon.simpleautodrop.config.*
import dev.nyon.simpleautodrop.screen.ConfigScreen
import kotlinx.coroutines.*
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.Items
import org.lwjgl.glfw.GLFW
import kotlin.time.Duration.Companion.milliseconds

lateinit var mcDispatcher: CoroutineDispatcher

object SimpleAutoDrop : ClientModInitializer {

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
    fun onTake() = runBlocking {
        if (!settings.enabled) return@runBlocking
        if (settings.activeArchives.isEmpty()) return@runBlocking
        val minecraft = Minecraft.getInstance()
        val player = minecraft.player ?: return@runBlocking
        if (jobWaiting) return@runBlocking
        jobWaiting = true

        launch(mcDispatcher) {
            delay(200.milliseconds)

            val screen = InventoryScreen(player)
            screen.menu.slots.forEachIndexed { _, slot ->
                if (blockedSlots.contains(slot.index)) return@forEachIndexed
                if (slot.item.item == Items.AIR || !itemIds.contains(
                        BuiltInRegistries.ITEM.getKey(slot.item.item).toString()
                    ) || !slot.hasItem()
                ) return@forEachIndexed
                minecraft.gameMode?.handleInventoryMouseClick(
                    screen.menu.containerId, slot.index, 1, ClickType.THROW, player
                )
            }

            jobWaiting = false
        }
    }

    override fun onInitializeClient() {
        mcDispatcher = Minecraft.getInstance().asCoroutineDispatcher()
    }
}