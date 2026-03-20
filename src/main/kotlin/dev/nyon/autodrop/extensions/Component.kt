package dev.nyon.autodrop.extensions

import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.item.Item

fun screenComponent(key: String, vararg objects: Any): MutableComponent {
    return Component.translatable("menu.autodrop.screen.$key", *objects)
}

val Item.narration: Component
    get() {
        return components().getOrDefault(DataComponents.ITEM_NAME, CommonComponents.EMPTY)
    }