package dev.nyon.autodrop

import dev.nyon.autodrop.config.*
import dev.nyon.autodrop.config.models.Config
import dev.nyon.autodrop.config.screen.createYaclScreen
import dev.nyon.konfig.config.config
import dev.nyon.konfig.config.loadConfig
import dev.nyon.konfig.config.saveConfig
import kotlinx.coroutines.*
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import kotlin.time.Duration.Companion.milliseconds

lateinit var mcDispatcher: CoroutineDispatcher
lateinit var mcScope: CoroutineScope
lateinit var minecraft: MinecraftClient

object AutoDrop : ClientModInitializer {

    private val toggleKeyBind = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "Toggle AutoDrop", InputUtil.Type.field_1668, GLFW.GLFW_KEY_J, "autodrop"
        )
    )

    private val menuKeyBind = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "Open GUI", InputUtil.Type.field_1668, GLFW.GLFW_KEY_O, "autodrop"
        )
    )

    fun tick(client: MinecraftClient) {
        if (toggleKeyBind.wasPressed()) {
            settings.enabled = !settings.enabled
            saveConfig(settings)
            client.inGameHud.setOverlayMessage(
                Text.translatable("menu.autodrop.name")
                    .append(Text.translatable(if (settings.enabled) "menu.autodrop.overlay.enabled" else "menu.autodrop.overlay.disabled"))
                    .withColor(0xF99147),
                false
            )
        }
        if (menuKeyBind.wasPressed()) client.setScreen(createYaclScreen(null))
    }

    private var jobWaiting = false
    fun onTake() = runBlocking {
        if (!settings.enabled) return@runBlocking
        if (settings.activeArchives.isEmpty()) return@runBlocking
        val player = minecraft.player ?: return@runBlocking
        if (jobWaiting) return@runBlocking
        jobWaiting = true

        mcScope.launch {
            delay(settings.dropDelay.milliseconds)

            val screen = InventoryScreen(player)
            screen.screenHandler.slots.filter {
                it.hasStack() && !blockedSlots.contains(it.index) && currentItems.contains(it.stack.item)
            }.forEachIndexed { _, slot ->
                minecraft.interactionManager?.clickSlot(
                    screen.screenHandler.syncId, slot.index, 1, SlotActionType.THROW, player
                )
            }

            jobWaiting = false
        }
    }

    override fun onInitializeClient() {
        minecraft = MinecraftClient.getInstance()
        mcDispatcher = minecraft.asCoroutineDispatcher()
        mcScope = CoroutineScope(SupervisorJob() + mcDispatcher)

        config("simpleautodrop", 1, Config()) { jsonTree, version -> migrate(jsonTree, version) }
        settings = loadConfig<Config>() ?: error("No config settings provided to load config!")

        reloadArchiveProperties()
    }
}