package dev.nyon.autodrop.config

import kotlinx.serialization.Serializable

/**
 * Represents the root config.
 *
 * @param enabled defines whether the mod is enabled or not.
 * @param triggerConfig defines the triggers for clearing the inventory of unwanted items.
 * @param profiles define the profiles, represented by [Profile]. There are none by default, so the workspace
 * creates the first one with a name in the player's language.
 * @param dropDelay defines the delay in milliseconds between a trigger and dropping unwanted items.
 */
@Serializable
data class Config(
    var enabled: Boolean = true,
    val triggerConfig: TriggerConfig = TriggerConfig(),
    val profiles: MutableList<Profile> = mutableListOf(),
    var dropDelay: Long = 200
) {
    fun deepCopy() =
        copy(triggerConfig = triggerConfig.copy(), profiles = profiles.map { it.deepCopy() }.toMutableList())
}

/**
 * Represents the trigger config.
 *
 * @param onPickup defines whether inventory changes should trigger dropping unwanted items.
 * @param onSneak defines whether starting to crouch triggers dropping unwanted items. It never restricts other triggers.
 */
@Serializable
data class TriggerConfig(var onPickup: Boolean = true, var onSneak: Boolean = false)

/** InventoryMenu indices of the helmet, chestplate, leggings and boots slots. */
val ARMOR_SLOTS = setOf(5, 6, 7, 8)

/** Defines whether a profile drops its listed items or keeps them and drops unlisted items. */
@Serializable
enum class ProfileMode {
    DROP,
    KEEP
}

/** Defines whether a rule drops every matching item, half of each stack, or one exact total across stacks. */
@Serializable
enum class QuantityMode {
    ALL,
    HALF,
    EXACT
}

/**
 * Represents a profile and its item and slot rules.
 *
 * @param enabled defines whether the profile is enabled. New profiles start disabled.
 * @param name is the identifier displayed for the profile.
 * @param entries define the item rules, represented by [ProfileRule].
 * @param protectedSlots define the player slots protected across all enabled profiles. These are
 * InventoryMenu indices: 5–8 armor, 9–35 inventory, 36–44 hotbar, and 45 offhand. New profiles protect the
 * armor slots, so worn armor is never dropped unless the player unprotects it.
 * @param mode defines whether listed items should be dropped or kept.
 * @param keepReviewed defines whether the current keep list has been reviewed before enabling it.
 */
@Serializable
data class Profile(
    var enabled: Boolean = false,
    var name: String,
    val entries: MutableList<ProfileRule> = mutableListOf(),
    val protectedSlots: MutableSet<Int> = ARMOR_SLOTS.toMutableSet(),
    var mode: ProfileMode = ProfileMode.DROP,
    var keepReviewed: Boolean = false
) {
    fun deepCopy() =
        copy(entries = entries.map { it.copy() }.toMutableList(), protectedSlots = protectedSlots.toMutableSet())
}

/**
 * Represents an item rule for identifying items to drop or keep.
 *
 * @param type is the registry ID of the item, e.g. minecraft:stone. Null explicitly matches any item;
 * unknown registry IDs remain IDs and never become wildcards.
 * @param predicate is the item data to filter for, using Minecraft's item-predicate syntax.
 * @param quantityMode defines how many matching items to drop. Keep rules protect every matching amount.
 * @param quantity is the exact total to drop across eligible stacks for one trigger when using [QuantityMode.EXACT].
 * @param minimumInventory is the matching total required in the player inventory before dropping,
 * including protected slots. It is a condition, not an amount to reserve.
 */
@Serializable
data class ProfileRule(
    var type: String? = null,
    var predicate: String = "",
    var quantityMode: QuantityMode = QuantityMode.ALL,
    var quantity: Int = 16,
    var minimumInventory: Int = 0
)
