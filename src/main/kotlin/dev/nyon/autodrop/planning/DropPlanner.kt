package dev.nyon.autodrop.planning

import dev.nyon.autodrop.config.*

/** Only player-owned inventory slots belong here; external containers and the cursor never do. */
data class InventoryStack(val slot: Int, val type: String, val count: Int, val canDrop: Boolean = true)

enum class Reason {
    DISABLED, PROTECTED_SLOT, KEEP_RULE, NO_RULE, UNAVAILABLE_SLOT, BELOW_MINIMUM, EXACT_INSUFFICIENT,
    BATCH_ALLOCATED, DROP_LISTED, DROP_UNLISTED, EMPTY_KEEP, INVALID_PREDICATE
}

/**
 * How a slot's planned amount is dropped. Whole stacks are thrown like Ctrl+Q and the larger half is taken with a
 * right click and dropped from the cursor. The cursor cannot split off any other amount, so those are thrown one
 * item at a time, one packet per item.
 */
enum class DropMethod { NONE, STACK, HALF, SINGLE_ITEMS }

data class SlotDecision(
    val slot: Int,
    val count: Int,
    val dropCount: Int = 0,
    val reason: Reason,
    val profiles: List<String> = emptyList(),
    val total: Long = 0,
    val required: Int = 0
) {
    val remaining: Int get() = count - dropCount
    val method: DropMethod get() = when {
        dropCount <= 0 -> DropMethod.NONE
        dropCount >= count -> DropMethod.STACK
        dropCount == (count + 1) / 2 -> DropMethod.HALF
        else -> DropMethod.SINGLE_ITEMS
    }
}

data class DropPlan(val slots: List<SlotDecision>) {
    val dropCount: Int get() = slots.sumOf { it.dropCount }
    val stackCount: Int get() = slots.count { it.dropCount > 0 }
    fun at(slot: Int) = slots.firstOrNull { it.slot == slot }
}

/**
 * One shared, pure planner for execution and preview.
 *
 * A stack is a drop target when it matches any enabled drop rule, and every rule matching it applies its limits:
 * all of their minimums must be met, a half-stack rule halves it, and each exact rule counts it toward its own
 * total. The strictest matching rule therefore decides how much of a shared stack drops. An exact rule without
 * enough eligible items skips its stacks. Slots are allocated in InventoryMenu order.
 */
object DropPlanner {
    fun stopped(inventory: List<InventoryStack>, reason: Reason) = DropPlan(inventory.map {
        SlotDecision(it.slot, it.count, reason = reason)
    })

    fun plan(
        config: Config,
        inventory: List<InventoryStack>,
        predicateMatches: (ProfileRule, InventoryStack) -> Boolean = { rule, _ -> rule.predicate.isBlank() || rule.predicate == "[]" }
    ): DropPlan {
        val stacks = inventory.filter { it.count > 0 }.sortedBy { it.slot }
        if (!config.enabled) return stopped(stacks, Reason.DISABLED)
        val profiles = config.profiles.filter { it.enabled }
        if (profiles.any { it.mode == ProfileMode.KEEP && it.entries.isEmpty() }) return stopped(stacks, Reason.EMPTY_KEEP)
        fun matches(rule: ProfileRule, stack: InventoryStack) =
            (rule.type == null || rule.type == stack.type) && predicateMatches(rule, stack)
        val keeps = profiles.filter { it.mode == ProfileMode.KEEP }
        val decisions = stacks.associate { stack ->
            val protecting = profiles.filter { stack.slot in it.protectedSlots }
            val keeping = keeps.filter { profile -> profile.entries.any { matches(it, stack) } }
            stack.slot to when {
                protecting.isNotEmpty() -> SlotDecision(stack.slot, stack.count, reason = Reason.PROTECTED_SLOT, profiles = protecting.map { it.name })
                keeping.isNotEmpty() -> SlotDecision(stack.slot, stack.count, reason = Reason.KEEP_RULE, profiles = keeping.map { it.name })
                !stack.canDrop -> SlotDecision(stack.slot, stack.count, reason = Reason.UNAVAILABLE_SLOT)
                else -> SlotDecision(stack.slot, stack.count, reason = Reason.NO_RULE)
            }
        }.toMutableMap()
        class Rule(val profile: Profile, val value: ProfileRule, val matching: List<InventoryStack>) {
            val total = matching.sumOf { it.count.toLong() }
            val exact = value.quantityMode == QuantityMode.EXACT
        }
        val rules = profiles.filter { it.mode == ProfileMode.DROP }.flatMap { profile ->
            profile.entries.map { rule -> Rule(profile, rule, stacks.filter { matches(rule, it) }) }
        }
        // Protected, kept and unavailable stacks still count toward minimums, but only the others are targets.
        val targets = stacks.filter { decisions.getValue(it.slot).reason == Reason.NO_RULE }
            .associateWith { stack -> rules.filter { stack in it.matching } }
            .filterValues { it.isNotEmpty() }
        fun cap(stack: InventoryStack, matched: List<Rule>) = when {
            matched.none { it.value.quantityMode == QuantityMode.HALF } -> stack.count
            // Preserve the legacy right-click split: ceil(n / 2), and leave singletons alone.
            stack.count > 1 -> (stack.count + 1) / 2
            else -> 0
        }
        val waiting = rules.filter { it.total < it.value.minimumInventory }.toSet()
        // Skipping an exact rule's stacks can leave another exact rule sharing them short as well.
        val insufficient = mutableMapOf<Rule, Long>()
        do {
            val eligible = targets.filterValues { matched -> matched.none { it in waiting || it in insufficient } }
            val short = rules.filter { it.exact && it !in waiting && it !in insufficient }.associateWith { rule ->
                eligible.entries.filter { (_, matched) -> rule in matched }.sumOf { (stack, matched) -> cap(stack, matched).toLong() }
            }.filter { (rule, capacity) -> capacity < rule.value.quantity }
            insufficient += short
        } while (short.isNotEmpty())
        val budgets = rules.filter { it.exact }.associateWith { it.value.quantity.toLong() }.toMutableMap()
        targets.forEach { (stack, matched) ->
            val names = matched.map { it.profile.name }.distinct()
            val below = matched.firstOrNull { it in waiting }
            val short = matched.firstOrNull { it in insufficient }
            decisions[stack.slot] = when {
                below != null -> SlotDecision(stack.slot, stack.count, reason = Reason.BELOW_MINIMUM, profiles = names,
                    total = below.total, required = below.value.minimumInventory)
                short != null -> SlotDecision(stack.slot, stack.count, reason = Reason.EXACT_INSUFFICIENT, profiles = names,
                    total = insufficient.getValue(short), required = short.value.quantity)
                else -> {
                    val exact = matched.filter { it.exact }
                    val amount = (exact.map { budgets.getValue(it) } + cap(stack, matched).toLong()).min().coerceAtLeast(0).toInt()
                    exact.forEach { budgets[it] = budgets.getValue(it) - amount }
                    SlotDecision(stack.slot, stack.count, amount, if (amount > 0) Reason.DROP_LISTED else Reason.BATCH_ALLOCATED,
                        names, matched.minOf { it.total })
                }
            }
        }
        if (keeps.isNotEmpty()) {
            // Invert the UNION of all enabled keep lists once. Stacks a drop rule matches keep that rule's limits.
            stacks.filter { decisions.getValue(it.slot).reason == Reason.NO_RULE }.forEach {
                decisions[it.slot] = SlotDecision(it.slot, it.count, it.count, Reason.DROP_UNLISTED, keeps.map { p -> p.name })
            }
        }
        return DropPlan(stacks.map { decisions.getValue(it.slot) })
    }
}
