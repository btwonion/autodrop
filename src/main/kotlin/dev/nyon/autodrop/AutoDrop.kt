package dev.nyon.autodrop

import com.mojang.blaze3d.platform.InputConstants
import dev.nyon.autodrop.config.*
import dev.nyon.autodrop.extensions.DataComponentPatchSerializer
import dev.nyon.autodrop.extensions.ItemSerializer
import dev.nyon.konfig.config.config
import dev.nyon.konfig.config.loadConfig
import dev.nyon.konfig.config.saveConfig
import kotlinx.coroutines.*
import kotlinx.serialization.modules.SerializersModule
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.Item
import org.lwjgl.glfw.GLFW
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration.Companion.milliseconds

lateinit var mcScope: CoroutineScope
lateinit var minecraft: Minecraft

object AutoDrop : ClientModInitializer {
    private val toggleKeyBind =
        KeyMapping("key.autodrop.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.autodrop.category")
    private val menuKeyBind =
        KeyMapping("key.autodrop.gui", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, "key.autodrop.category")

    fun tick(client: Minecraft) {
        if (toggleKeyBind.consumeClick()) {
            config.enabled = !config.enabled
            saveConfig(config)
            client.gui.setOverlayMessage(
                Component.translatable("menu.autodrop.name").append(" ").append(
                    Component.translatable(if (config.enabled) "menu.autodrop.overlay.enabled" else "menu.autodrop.overlay.disabled")
                ).withStyle(Style.EMPTY.withColor(0xF99147)), false
            )
            if (config.enabled) invokeAutodrop()
        }
        // TODO: screen if (menuKeyBind.consumeClick()) client.setScreen(createYaclScreen(null))
    }

    private var jobWaiting = false

    fun invokeAutodrop() = runBlocking {
        if (jobWaiting) return@runBlocking
        if (!config.enabled) return@runBlocking
        if (currentItems.isEmpty()) return@runBlocking
        val player = minecraft.player ?: return@runBlocking
        if (config.triggerConfig.onSneak != player.isCrouching) return@runBlocking
        jobWaiting = true

        mcScope.launch {
            delay(config.dropDelay.milliseconds)

            val screen = InventoryScreen(player)
            screen.menu.slots.filter {
                it.hasItem() && !ignoredSlots.contains(it.index) && it.container is Inventory
            }.forEach { slot ->
                val itemStack = slot.item
                val isValid = currentItems.any { identification ->
                    val typeValid = identification.type == null || itemStack.item == identification.type
                    val amountValid = itemStack.count >= identification.amount
                    val componentValid = identification.components.isEmpty || identification.components.entrySet()
                        .all { (key, component) ->
                            itemStack.get(key) == component.getOrNull()
                        }

                    typeValid && amountValid && componentValid
                }

                if (!isValid) return@forEach

                minecraft.gameMode?.handleInventoryMouseClick(
                    screen.menu.containerId, slot.index, 1, ClickType.THROW, player
                )
            }

            jobWaiting = false
        }
    }

    override fun onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(menuKeyBind)
        KeyBindingHelper.registerKeyBinding(toggleKeyBind)
        minecraft = Minecraft.getInstance()
        mcScope = CoroutineScope(SupervisorJob() + minecraft.asCoroutineDispatcher())

        config(FabricLoader.getInstance().configDir.resolve("autodrop.json"), 1, Config(), jsonBuilder = {
            serializersModule = SerializersModule {
                contextual(Item::class, ItemSerializer)
                contextual(DataComponentPatch::class, DataComponentPatchSerializer)
            }
        }) { jsonTree, version -> migrate(jsonTree, version) }
        config = loadConfig<Config>()

        reloadArchiveProperties()
    }
}
