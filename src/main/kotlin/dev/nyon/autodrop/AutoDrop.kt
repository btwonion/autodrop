package dev.nyon.autodrop

import com.mojang.blaze3d.platform.InputConstants
import dev.nyon.autodrop.config.blockedSlots
import dev.nyon.autodrop.config.createYaclScreen
import dev.nyon.autodrop.config.currentItems
import dev.nyon.autodrop.config.migrate
import dev.nyon.autodrop.config.models.Config
import dev.nyon.autodrop.config.reloadArchiveProperties
import dev.nyon.autodrop.config.settings
import dev.nyon.konfig.config.config
import dev.nyon.konfig.config.loadConfig
import dev.nyon.konfig.config.saveConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.ClickType
import org.lwjgl.glfw.GLFW
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.notExists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.milliseconds

lateinit var mcDispatcher: CoroutineDispatcher
lateinit var mcScope: CoroutineScope
lateinit var minecraft: Minecraft

object AutoDrop : ClientModInitializer {
    private val toggleKeyBind =
        KeyBindingHelper.registerKeyBinding(
            KeyMapping(
                "Toggle AutoDrop",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "autodrop"
            )
        )

    private val menuKeyBind =
        KeyBindingHelper.registerKeyBinding(
            KeyMapping(
                "Open GUI",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "autodrop"
            )
        )

    fun tick(client: Minecraft) {
        if (toggleKeyBind.consumeClick()) {
            settings.enabled = !settings.enabled
            saveConfig(settings)
            client.gui.setOverlayMessage(
                Component.translatable("menu.autodrop.name").append(" ")
                    .append(
                        Component.translatable(if (settings.enabled) "menu.autodrop.overlay.enabled" else "menu.autodrop.overlay.disabled")
                    )
                    .withColor(0xF99147),
                false
            )
            if (settings.enabled) onTake()
        }
        if (menuKeyBind.consumeClick()) client.setScreen(createYaclScreen(null))
    }

    private var jobWaiting = false

    fun onTake() =
        runBlocking {
            if (!settings.enabled) return@runBlocking
            if (settings.activeArchives.isEmpty()) return@runBlocking
            val player = minecraft.player ?: return@runBlocking
            if (jobWaiting) return@runBlocking
            jobWaiting = true

            mcScope.launch {
                delay(settings.dropDelay.milliseconds)

                val screen = InventoryScreen(player)
                screen.menu.slots.filter {
                    it.hasItem() && !blockedSlots.contains(it.index) && currentItems.contains(it.item.item) && it.container is Inventory
                }.forEach { slot ->
                    minecraft.gameMode?.handleInventoryMouseClick(
                        screen.menu.containerId,
                        slot.index,
                        1,
                        ClickType.THROW,
                        player
                    )
                }

                jobWaiting = false
            }
        }

    override fun onInitializeClient() {
        minecraft = Minecraft.getInstance()
        mcDispatcher = minecraft.asCoroutineDispatcher()
        mcScope = CoroutineScope(SupervisorJob() + mcDispatcher)

        val oldConfigFile = FabricLoader.getInstance().configDir.resolve("simpleautodrop.json")
        val newConfigFile = FabricLoader.getInstance().configDir.resolve("autodrop.json")
        if (oldConfigFile.exists()) {
            if (newConfigFile.notExists()) newConfigFile.createFile()
            newConfigFile.writeText(oldConfigFile.readText())
            oldConfigFile.deleteIfExists()
        }
        config(newConfigFile, 1, Config()) { jsonTree, version -> migrate(jsonTree, version) }
        settings = loadConfig<Config>() ?: error("No config settings provided to load config!")

        reloadArchiveProperties()
    }
}
