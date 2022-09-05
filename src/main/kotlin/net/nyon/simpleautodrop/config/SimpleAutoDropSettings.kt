package net.nyon.simpleautodrop.config

import dev.isxander.yacl.api.ConfigCategory
import dev.isxander.yacl.api.Option
import dev.isxander.yacl.api.YetAnotherConfigLib
import kotlinx.serialization.Serializable
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.item.Item
import net.nyon.simpleautodrop.util.ItemSerializer
import net.silkmc.silk.core.text.literalText

var settings: SimpleAutoDropSettings = SimpleAutoDropSettings(true, arrayListOf())

@Serializable
data class SimpleAutoDropSettings(
    var enabled: Boolean, var items: ArrayList<@Serializable(with = ItemSerializer::class) Item>
)

fun configGui(): Screen {
    return YetAnotherConfigLib.createBuilder().title(literalText("SimpleAutoDrop")).category(
        ConfigCategory.createBuilder().name(literalText("Settings"))
            .tooltip(literalText("Click to open the SimpleAutoDrop settings")).option(
                Option.createBuilder(Boolean::class.java).name(literalText("Auto drop"))
                    .tooltip(literalText("Click to toggle auto drop"))
                    .binding(true, { settings.enabled }, { !settings.enabled }).build()
            ).build()
    ).build().generateScreen(null)
}