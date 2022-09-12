package dev.nyon.simpleautodrop.config

import dev.nyon.simpleautodrop.util.ItemSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.commands.arguments.item.ItemInput
import net.minecraft.world.item.Item
import net.silkmc.silk.commands.clientCommand
import net.silkmc.silk.commands.player
import net.silkmc.silk.core.text.literalText

var settings: SimpleAutoDropSettings = SimpleAutoDropSettings(true, arrayListOf())
var itemIds: MutableList<Int> = settings.items.map { Item.getId(it) }.toMutableList()

@Serializable
@SerialName("autodrop_settings")
data class SimpleAutoDropSettings(
    var enabled: Boolean, var items: ArrayList<@Serializable(with = ItemSerializer::class) Item>
)

val autoDropCommand = clientCommand("autodrop") {

    literal("add") {
        argument<ItemInput>("item") { itemInput ->
            runs {
                val item = itemInput().item
                if (settings.items.contains(item)) {
                    source.player.sendSystemMessage(literalText("This item is already enabled for auto drop!") {
                        color = 0xEE042A
                    })
                    return@runs
                }
                settings.items += item
                saveConfig()
                itemIds += Item.getId(item)
                source.player.sendSystemMessage(literalText("This item was enabled for auto drop!") {
                    color = 0x1A631F
                })
            }
        }
    }

    literal("remove") {
        argument<ItemInput>("item") { itemInput ->
            runs {
                val item = itemInput().item
                if (!settings.items.contains(item)) {
                    source.player.sendSystemMessage(literalText("This item is already disabled for auto drop!") {
                        color = 0xEE042A
                    })
                    return@runs
                }
                settings.items -= item
                saveConfig()
                itemIds -= Item.getId(item)
                source.player.sendSystemMessage(literalText("This item was disabled for auto drop!") {
                    color = 0x1A631F
                })
            }
        }
    }

    literal("list") {
        runs {
            if (settings.items.isEmpty()) {
                source.player.sendSystemMessage(literalText("You don't use auto drop for any item!") {
                    color = 0x50F9FB
                })
                return@runs
            }
            source.player.sendSystemMessage(literalText("You have the following items enabled for auto drop:") {
                color = 0x869097
                emptyLine()
                settings.items.map { it.description.string }.forEach {
                    text("- $it") {
                        color = 0x50F9FB
                    }
                    newLine()
                }
            })
        }
    }
}