package dev.nyon.autodrop.planning

/** Container identity, not a screen's numerical slot index, determines inventory ownership. */
internal fun <T> remapPlayerSlot(
    container: Any, inventoryIndex: Int, slots: Iterable<T>, slotContainer: (T) -> Any, slotInventoryIndex: (T) -> Int
): T? = slots.firstOrNull { slotContainer(it) === container && slotInventoryIndex(it) == inventoryIndex }
