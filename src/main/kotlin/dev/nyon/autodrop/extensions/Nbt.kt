package dev.nyon.autodrop.extensions

import net.minecraft.nbt.CompoundTag

/*? if >=1.20.5 {*/
import net.minecraft.core.component.DataComponentMap
import net.minecraft.core.component.DataComponentPatch
/*?}*/

typealias StoredComponents = /*? if >=1.21 {*/ DataComponentPatch /*?} else if >=1.20.5 {*/ /*DataComponentMap *//*?} else {*/ /*CompoundTag *//*?}*/

val emptyStoredComponents: StoredComponents = /*? if >=1.21 {*/ DataComponentPatch.EMPTY /*?} else if >=1.20.5 {*/ /*DataComponentMap.EMPTY *//*?} else {*/ /*CompoundTag() *//*?}*/