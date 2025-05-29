package dev.nyon.autodrop

import com.mojang.blaze3d.platform.InputConstants
import dev.nyon.autodrop.config.ItemIdentifier
import dev.nyon.autodrop.config.config
import dev.nyon.autodrop.config.currentItems
import dev.nyon.autodrop.config.ignoredSlots
import dev.nyon.autodrop.config.screen.root.ArchiveScreen
import dev.nyon.konfig.config.saveConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
//? if >=1.20.5
import net.minecraft.world.item.enchantment.ItemEnchantments
import org.lwjgl.glfw.GLFW
import kotlin.time.Duration.Companion.milliseconds

lateinit var mcScope: CoroutineScope
lateinit var minecraft: Minecraft

object AutoDrop {
    private var jobWaiting = false

    /**
     * Filters slots for items matching the filter and drops them after a specified delay.
     * The job is only executed when no other job is currently running and the player matches the configured criteria.
     */
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
                it.hasItem() && !ignoredSlots.contains(it.correctSlotId())
            }.forEach { slot ->
                val itemStack = slot.item
                val isValid = currentItems.any { identifier ->
                    val typeValid = identifier.type == null || itemStack.item == identifier.type
                    val amountValid = itemStack.count >= identifier.amount
                    val componentValid = isComponentValid(itemStack, identifier)

                    typeValid && amountValid && componentValid
                }

                if (isValid) minecraft.gameMode?.handleInventoryMouseClick(
                    player.containerMenu.containerId, slot.correctSlotId(), 1, ClickType.THROW, player
                )
            }
        }.invokeOnCompletion {
            jobWaiting = false
        }
    }

    val toggleKeyBind by lazy {
        KeyMapping(
            "key.autodrop.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.autodrop.category"
        )
    }
    val menuKeyBind by lazy {
        KeyMapping(
            "key.autodrop.gui", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, "key.autodrop.category"
        )
    }

    /**
     * Handles the keybinds.
     */
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

    /**
     * Checks whether the components of the item stack match the identifier.
     */
    fun isComponentValid(itemStack: ItemStack, identifier: ItemIdentifier): Boolean {
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

        return componentValid
    }

    /**
     * Transforms the requested slot index to the index matching the schema of the player's inventory.
     */
    private fun Slot.correctSlotId(): Int { // The number of slots that are currently accessible to the player.
        // If no container is opened, it is expected to be a player's inventory menu with the inventory's index starting at 0.
        val openedContainerSize = minecraft.player?.containerMenu?.slots?.size ?: (0 + index)

        // The starting index of the actual inventory of the player.
        // In the case of the Crafter's and the player's inventory menu, the last slot is placed incorrectly, leading to wrong slot identification.
        // -> start slot is 9
        val invStartIndex = if (openedContainerSize == 46) 9 else openedContainerSize - 36

        // The index of the slot subtracted by the above 9 slots of the player's inventory menu.
        val flattenedInvIndex = index - 9
        return invStartIndex + flattenedInvIndex
    }
}
