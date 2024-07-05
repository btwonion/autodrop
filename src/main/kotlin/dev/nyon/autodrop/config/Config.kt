package dev.nyon.autodrop.config

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.world.item.Item

/**
 * Represents the root config.
 *
 * @param enabled defines whether the mod is enabled or not.
 * @param triggerConfig defines the triggers for clearing the inventory of unwanted items.
 * @param archives define the archives, represented by [dev.nyon.autodrop.config.Archive].
 * @param dropDelay defines the delay between trigger and drop of unwanted items.
 */

@Serializable
data class Config(
    var enabled: Boolean = true,
    val triggerConfig: TriggerConfig = TriggerConfig(),
    val archives: MutableList<Archive> = mutableListOf(),
    var dropDelay: Long = 200
)

/**
 * Represents the trigger config.
 *
 * @param onPickup defines whether the inventory should be cleared on item pickups.
 * @param onSneak defines whether the inventory should be cleared on sneak.
 * @param onJump defines whether the inventory should be cleared on jump.
 * @param onSlotSwitch defines whether the inventory should be cleared on slot switch in the hotbar.
 */
@Suppress("SpellCheckingInspection")
@Serializable
data class TriggerConfig(
    val onPickup: Boolean = true,
    val onSneak: Boolean = false,
    val onJump: Boolean = false,
    val onSlotSwitch: Boolean = false
)

/**
 * Represents a config entry for an archive.
 *
 * @param enabled defines whether the archive is enabled.
 * @param name is the identifier of the archive.
 * @param entries define the entries of the archive. Those are represented by [dev.nyon.autodrop.config.ItemIdentification].
 * @param ignoredSlots define the slots that are ignored by the mod and thus don't get cleared automatically.
 */
@Serializable
data class Archive(
    val enabled: Boolean = true,
    val name: String,
    var entries: MutableList<ItemIdentification>,
    var ignoredSlots: MutableList<Int>
)

/**
 * Represents and entry of an archive for identification of items that are valid to be dropped.
 *
 * @param type is the type of the item and serialized as a [net.minecraft.resources.ResourceLocation], e.g. minecraft:stone.
 * @param components is the item data. The format should match the item argument in the commands, that are read through [net.minecraft.commands.arguments.item.ItemParser].
 * @param amount is the amount of the item that is required for the item to be dropped.
 */
@Serializable
data class ItemIdentification(
    var type: @Contextual Item?, var components: @Contextual DataComponentPatch, var amount: Int
)
