package dev.nyon.autodrop.planning

import com.mojang.brigadier.StringReader
import dev.nyon.autodrop.AutoDrop
import dev.nyon.autodrop.config.Config
import dev.nyon.autodrop.extensions.matchItemPredicate
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import java.util.function.Predicate

object PredicateCache {
    private var connection: Any? = null
    private val cache = mutableMapOf<String, Result<Predicate<ItemStack>>>()

    fun compile(text: String): Result<Predicate<ItemStack>> {
        val current = AutoDrop.minecraft.connection
        if (connection !== current) {
            connection = current; cache.clear()
        }
        if (text.isBlank() || text == "[]") return Result.success(Predicate { true })
        cache[text]?.let { return it } // Typing a predicate compiles every intermediate text; evict the oldest entry only on a miss.
        if (cache.size >= 256) cache.remove(cache.keys.first())
        return cache.getOrPut(text) {
            runCatching {
                val reader = StringReader(text.trim().matchItemPredicate())
                val predicate = AutoDrop.itemPredicateArgument.parse(reader)
                require(!reader.canRead()) { "Unexpected text at position ${reader.cursor}" }
                predicate
            }
        }
    }
}

data class PlayerInventorySnapshot(
    val menu: AbstractContainerMenu,
    val stacks: List<InventoryStack>,
    val items: Map<Int, ItemStack>,
    val targets: Map<Int, Slot>
)

object MinecraftPlanner {
    fun snapshot(): PlayerInventorySnapshot? {
        val player = AutoDrop.minecraft.player ?: return null
        val menu = player.containerMenu
        val items = mutableMapOf<Int, ItemStack>()
        val targets = mutableMapOf<Int, Slot>()
        val stacks = player.inventoryMenu.slots.filter { it.container === player.inventory && it.hasItem() }
            .map { slot -> // Match container identity AND inventory index, never reuse the InventoryMenu index in a chest/crafting menu.
                val target = remapPlayerSlot(
                    slot.container,
                    slot.containerSlot,
                    menu.slots,
                    { it.container },
                    { it.containerSlot })
                items[slot.index] = slot.item.copy()
                if (target != null) targets[slot.index] = target
                InventoryStack(
                    slot.index,
                    BuiltInRegistries.ITEM.getKey(slot.item.item).toString(),
                    slot.item.count,
                    target != null && target.mayPickup(player) && target.isActive && menu.carried.isEmpty && player.canDropItems()
                )
            }
        return PlayerInventorySnapshot(menu, stacks, items, targets)
    }

    fun plan(config: Config, snapshot: PlayerInventorySnapshot): DropPlan {
        if (!config.enabled) return DropPlanner.stopped(snapshot.stacks, Reason.DISABLED)
        val predicates = config.profiles.filter { it.enabled }.flatMap { it.entries }
            .associate { it.predicate to PredicateCache.compile(it.predicate) }
        if (predicates.values.any { it.isFailure }) return DropPlanner.stopped(
            snapshot.stacks,
            Reason.INVALID_PREDICATE
        )
        return DropPlanner.plan(config, snapshot.stacks) { rule, stack ->
            predicates.getValue(rule.predicate).getOrThrow().test(snapshot.items.getValue(stack.slot))
        }
    }
}
