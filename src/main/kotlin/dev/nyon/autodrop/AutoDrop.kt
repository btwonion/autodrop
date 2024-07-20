package dev.nyon.autodrop

import com.mojang.blaze3d.platform.InputConstants
import dev.nyon.autodrop.config.*
import dev.nyon.autodrop.config.screen.root.ArchiveScreen
import dev.nyon.autodrop.extensions.DataComponentPatchSerializer
import dev.nyon.autodrop.extensions.ItemSerializer
import dev.nyon.autodrop.extensions.StoredComponents
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
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.Item
//? if >=1.20.5
import net.minecraft.world.item.enchantment.ItemEnchantments
import org.lwjgl.glfw.GLFW
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
        if (menuKeyBind.consumeClick()) client.setScreen(ArchiveScreen(null))
    }

    private var jobWaiting = false

    fun invokeAutodrop() = runBlocking {
        if (jobWaiting) return@runBlocking
        if (!config.enabled) return@runBlocking
        if (currentItems.isEmpty()) return@runBlocking
        val player = minecraft.player ?: return@runBlocking
        if (config.triggerConfig.onSneak && !player.isCrouching) return@runBlocking
        jobWaiting = true

        mcScope.launch {
            delay(config.dropDelay.milliseconds)

            val screen = InventoryScreen(player)
            screen.menu.slots.filter {
                it.hasItem() && !ignoredSlots.contains(it.index) && it.container is Inventory
            }.forEach { slot ->
                val itemStack = slot.item
                val isValid = currentItems.any { identifier ->
                    val typeValid = identifier.type == null || itemStack.item == identifier.type
                    val amountValid = itemStack.count >= identifier.amount

                    /*? if >=1.21 {*/
                    
                    val componentValid =
                        identifier.components.isEmpty || identifier.components.entrySet().all { (key, component) ->
                            val identifierComponent = component.get()
                            val itemComponent = itemStack.get(key) ?: return@all false

                            if (identifierComponent as? ItemEnchantments != null && itemComponent as? ItemEnchantments != null) {
                                return@all identifierComponent.entrySet()
                                    .all { entry -> // How tf is minecraft too bad to create a simple equals check - or is it me?
                                        itemComponent.entrySet().map { it.key.value().description.string }
                                            .contains(entry.key.value().description.string)
                                    }
                            }

                            itemComponent == identifierComponent
                        }
                    /*?} else if >=1.20.5 {*/
                    /*val componentValid = identifier.components.isEmpty || identifier.components.all { component ->
                        val identifierComponent = component.value
                        val itemComponent = itemStack.get(component.type) ?: return@all false

                        if (identifierComponent as? ItemEnchantments != null && itemComponent as? ItemEnchantments != null) {
                            return@all identifierComponent.entrySet()
                                .all { entry -> // How tf is minecraft too bad to create a simple equals check - or is it me?
                                    itemComponent.entrySet().map { it.key.value().descriptionId }
                                        .contains(entry.key.value().descriptionId)
                                }
                        }

                        itemComponent == identifierComponent
                    }
                    *//*?} else {*//*
                    val componentValid = identifier.components.allKeys.all { key ->
                        if (itemStack.tag == null) return@all false
                        itemStack.tag!!.get(key) == identifier.components.get(key)
                    }
                    *//*?}*/
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
                contextual(StoredComponents::class, DataComponentPatchSerializer)
            }
        }) { jsonTree, version -> migrate(jsonTree, version) }
        config = loadConfig<Config>()

        reloadArchiveProperties()
    }
}
