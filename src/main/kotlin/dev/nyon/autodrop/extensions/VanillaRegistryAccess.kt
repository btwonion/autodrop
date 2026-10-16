package dev.nyon.autodrop.extensions

import net.minecraft.core.HolderLookup
import net.minecraft.data.registries.VanillaRegistries

object VanillaRegistryAccess {
    private val lookup: HolderLookup.Provider by lazy {
        // Include dynamic vanilla registries (notably enchantments) for editing outside a world.
        //? if >=26.3
        VanillaRegistries.createWorldLookup()
        //? if <26.3
        //VanillaRegistries.createLookup()
    }
    fun createVanillaRegistryAccess(): HolderLookup.Provider = lookup
}
