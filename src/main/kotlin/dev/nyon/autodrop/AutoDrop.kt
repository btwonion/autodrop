package dev.nyon.autodrop

import dev.nyon.autodrop.config.ArchiveEntry
import dev.nyon.autodrop.config.config
import dev.nyon.autodrop.config.currentItems
import dev.nyon.autodrop.config.ignoredSlots
import dev.nyon.autodrop.extensions.matchItemPredicate
import dev.nyon.autodrop.extensions.stringReader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.arguments.item.ItemPredicateArgument
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import kotlin.time.Duration.Companion.milliseconds

lateinit var mcScope: CoroutineScope
lateinit var minecraft: Minecraft

object AutoDrop {
    private var jobWaiting = false
    val itemPredicateArgument: ItemPredicateArgument by lazy {
        ItemPredicateArgument(
            CommandBuildContext.simple(
                minecraft.player?.registryAccess() ?: return@lazy error("Cannot load local player."),
                FeatureFlags.DEFAULT_FLAGS
            )
        )
    }

    /**
     * Filters slots for items matching the filter and drops them after a specified delay.
     * The job is only executed when no other job is currently running
     * and the player matches the configured criteria.
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
                val validIdentifiers = currentItems.mapNotNull { identifier ->
                    val typeValid =
                        identifier.type == null || identifier.type == Items.AIR || itemStack.item == identifier.type
                    val amountValid = itemStack.count >= identifier.amount
                    val predicateValid = isPredicateValid(itemStack, identifier)

                    if (typeValid && amountValid && predicateValid) identifier
                    else null
                }
                val isValid = validIdentifiers.isNotEmpty()

                if (isValid) {
                    val shouldDropEverything = validIdentifiers.all(ArchiveEntry::dropEverything)
                    if (shouldDropEverything) {
                        minecraft.gameMode?.handleInventoryMouseClick(
                            player.containerMenu.containerId, slot.correctSlotId(), 1, ClickType.THROW, player
                        )
                        return@forEach
                    }
                    if (itemStack.count > 1) {
                        minecraft.gameMode?.handleInventoryMouseClick(
                            player.containerMenu.containerId, slot.correctSlotId(), 1, ClickType.PICKUP, player
                        )
                        minecraft.gameMode?.handleInventoryMouseClick(
                            player.containerMenu.containerId, -999, 0, ClickType.PICKUP, player
                        )
                    }
                }
            }
        }.invokeOnCompletion {
            jobWaiting = false
        }
    }

    /**
     * Checks whether the components of the item stack match the identifier.
     */
    fun isPredicateValid(itemStack: ItemStack, identifier: ArchiveEntry): Boolean {
        val predicateResult = itemPredicateArgument.parse(identifier.predicate.matchItemPredicate().stringReader())
        return predicateResult.test(itemStack)
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
