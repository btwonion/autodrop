package dev.nyon.autodrop

import dev.nyon.autodrop.config.config
import dev.nyon.autodrop.config.screen.DraftScreen
import dev.nyon.autodrop.extensions.VanillaRegistryAccess
import dev.nyon.autodrop.extensions.currentScreen
import dev.nyon.autodrop.planning.*
import kotlinx.coroutines.*
import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.arguments.item.ItemPredicateArgument
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.item.ItemStack
import kotlin.time.Duration.Companion.milliseconds

object AutoDrop {
    val minecraft: Minecraft by lazy { Minecraft.getInstance() }
    val mcScope: CoroutineScope by lazy {
        CoroutineScope(SupervisorJob() + minecraft.asCoroutineDispatcher())
    }

    private data class StackKey(val type: Any, val components: Any)

    private val triggers = DropTriggerState<StackKey>()
    private var pendingJob: Job? = null
    private var inventoryEvent = false
    private var lastPlayer: Any? = null
    val isEditing: Boolean get() = triggers.editing

    val itemPredicateArgument: ItemPredicateArgument
        get() = ItemPredicateArgument(
            CommandBuildContext.simple(
                minecraft.connection?.registryAccess() ?: VanillaRegistryAccess.createVanillaRegistryAccess(),
                FeatureFlags.DEFAULT_FLAGS
            )
        )

    fun setEditing(editing: Boolean) {
        cancelPending()
        triggers.setEditing(editing)
        inventoryEvent = false
    }

    fun cancelPending() {
        pendingJob?.cancel(); pendingJob = null
    }

    fun onInventoryChanged(inventory: Inventory) {
        if (inventory === minecraft.player?.inventory && !triggers.executing && !isEditing) inventoryEvent = true
    }

    /**
     * Queues one coroutine batch on the client thread and re-plans after the configured delay.
     * Further triggers are coalesced while that job is waiting or executing.
     */
    fun invokeAutodrop() {
        val player = minecraft.player ?: return
        if (pendingJob != null || triggers.executing || !config.enabled || isEditing) return
        val delay = config.dropDelay.coerceIn(0, Int.MAX_VALUE.toLong()).milliseconds
        val job = mcScope.launch(start = CoroutineStart.LAZY) {
            val ownJob = currentCoroutineContext().job
            try {
                delay(delay)
                if (minecraft.player !== player || !config.enabled || isEditing || minecraft.currentScreen is DraftScreen || !triggers.begin()) return@launch
                dropBatch()
            } finally {
                if (pendingJob === ownJob) pendingJob = null
            }
        } // Assign before starting, including when a zero delay completes immediately.
        pendingJob = job
        job.start()
    }

    /** Observes inventory changes on the client thread without scheduling work from our own drops. */
    fun tick() {
        val editing = minecraft.currentScreen is DraftScreen
        if (editing != isEditing) setEditing(editing)
        val player = minecraft.player
        if (lastPlayer !== player) {
            cancelPending(); lastPlayer = player; triggers.reset(); inventoryEvent = false
        }
        val snapshot = MinecraftPlanner.snapshot() ?: return
        val externalChange = triggers.observe(observe(snapshot))
        val trigger = externalChange && inventoryEvent && config.triggerConfig.onPickup && config.enabled
        inventoryEvent = false
        if (!config.enabled || isEditing) {
            cancelPending(); return
        }
        if (trigger) invokeAutodrop()
    }

    private fun dropBatch() {
        try {
            val player = minecraft.player ?: return
            val snapshot = MinecraftPlanner.snapshot() ?: return
            val plan = MinecraftPlanner.plan(config, snapshot)
            val drops = plan.slots.filter { it.dropCount > 0 }
            val gameMode = minecraft.gameMode
                ?: return // Preflight the entire batch, so an exact amount is never intentionally partially allocated.
            if (player.containerMenu !== snapshot.menu || !snapshot.menu.carried.isEmpty || drops.any {
                    val target = snapshot.targets[it.slot]
                    target == null || !target.mayPickup(player) || !ItemStack.matches(
                        target.item,
                        snapshot.items[it.slot]!!
                    )
                }) return
            val menu = snapshot.menu.containerId
            drops.forEach { decision ->
                val slot = snapshot.targets.getValue(decision.slot).index
                when (decision.method) {
                    DropMethod.STACK -> gameMode.handleContainerInput(menu, slot, 1, ContainerInput.THROW, player)
                    DropMethod.HALF -> { // Right click takes the larger half onto the empty cursor, then a click outside drops it.
                        gameMode.handleContainerInput(menu, slot, 1, ContainerInput.PICKUP, player)
                        gameMode.handleContainerInput(
                            menu,
                            AbstractContainerMenu.SLOT_CLICKED_OUTSIDE,
                            0,
                            ContainerInput.PICKUP,
                            player
                        )
                    } // The cursor cannot split off this amount; the rule editor and preview warn about it.
                    DropMethod.SINGLE_ITEMS -> repeat(decision.dropCount) {
                        gameMode.handleContainerInput(menu, slot, 0, ContainerInput.THROW, player)
                    }
                    DropMethod.NONE -> {}
                }
            }
            MinecraftPlanner.snapshot()?.let { triggers.afterOwnBatch(observe(it)) }
        } finally {
            inventoryEvent = false
            triggers.finish()
        }
    }

    private fun observe(snapshot: PlayerInventorySnapshot) = snapshot.items.mapValues { (_, stack) ->
        ObservedStack(StackKey(stack.item, stack.componentsPatch), stack.count)
    }
}
