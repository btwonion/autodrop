package dev.nyon.autodrop.extensions

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.item.Item

fun screenComponent(key: String, vararg objects: Any): MutableComponent {
    return Component.translatable("menu.autodrop.screen.$key", *objects)
}

val Item.narration: Component
    get() {
        //? if >=1.21.2
        /*return this.name*/

        //? if <1.21.2
        return Component.literal(this.description.toString())
    }