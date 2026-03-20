package dev.nyon.autodrop.extensions

import net.minecraft.core.RegistryAccess
import net.minecraft.core.registries.BuiltInRegistries

object VanillaRegistryAccess {
    fun createVanillaRegistryAccess(): RegistryAccess.ImmutableRegistryAccess {
        return RegistryAccess.ImmutableRegistryAccess(
            listOf(
                BuiltInRegistries.ITEM,
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                //? if >1.21.4
                BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE
                //? if <=1.21.4
                /*BuiltInRegistries.ITEM_SUB_PREDICATE_TYPE*/
            )
        )
    }
}