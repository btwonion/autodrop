package dev.nyon.simpleautodrop.config

import com.mojang.brigadier.context.CommandContext
import dev.nyon.simpleautodrop.util.ItemSerializer
import kotlinx.serialization.Serializable
import net.minecraft.commands.arguments.item.ItemInput
import net.minecraft.world.item.Item
import net.silkmc.silk.commands.ClientCommandSourceStack
import net.silkmc.silk.commands.clientCommand
import net.silkmc.silk.commands.player
import net.silkmc.silk.core.text.literalText

var settings: SimpleAutoDropSettings = SimpleAutoDropSettings(true, hashMapOf(), null)
var itemIds: MutableMap<String, MutableList<Int>> = mutableMapOf()

@Serializable
data class SimpleAutoDropSettings(
    var enabled: Boolean,
    var items: HashMap<String, MutableList<@Serializable(with = ItemSerializer::class) Item>>,
    var currentArchive: String?
)

val autoDropCommand = clientCommand("autodrop") {

    argument<String>("archive") { archiveArg ->
        suggestList { settings.items.keys }
        literal("add") {
            argument<ItemInput>("item") { itemInput ->
                runs {
                    val archive = archiveArg()
                    if (!archiveCheck(archive)) return@runs
                    val item = itemInput().item
                    if (settings.items[archive]?.contains(item) == true) {
                        source.player.sendSystemMessage(literalText("This item is already enabled for auto drop!") {
                            color = 0xEE042A
                        })
                        return@runs
                    }
                    settings.items[archive]?.add(item)
                    saveConfig()
                    itemIds[archive]?.add(Item.getId(item))
                    source.player.sendSystemMessage(literalText("This item was enabled for auto drop!") {
                        color = 0x1A631F
                    })
                }
            }
        }

        literal("remove") {
            argument<ItemInput>("item") { itemInput ->
                runs {
                    val archive = archiveArg()
                    if (!archiveCheck(archive)) return@runs
                    val item = itemInput().item
                    if (settings.items[archive]?.contains(item) == false) {
                        source.player.sendSystemMessage(literalText("This item is already disabled for auto drop!") {
                            color = 0xEE042A
                        })
                        return@runs
                    }
                    settings.items[archive]?.remove(item)
                    saveConfig()
                    itemIds[archive]?.remove(Item.getId(item))
                    source.player.sendSystemMessage(literalText("This item was disabled for auto drop!") {
                        color = 0x1A631F
                    })
                }
            }
        }

        literal("list") {
            runs {
                val archive = archiveArg()
                if (!archiveCheck(archive)) return@runs
                if (settings.items.isEmpty()) {
                    source.player.sendSystemMessage(literalText("You don't use auto drop for any item!") {
                        color = 0x50F9FB
                    })
                    return@runs
                }
                source.player.sendSystemMessage(literalText("You have the following items enabled for auto drop:") {
                    color = 0x869097
                    emptyLine()
                    settings.items[archive]?.map { it.description.string }?.forEach {
                        text("- $it") {
                            color = 0x50F9FB
                        }
                        newLine()
                    }
                })
            }
        }

        literal("create") {
            runs {
                val archive = archiveArg()
                if (settings.items.containsKey(archive)) {
                    source.player.sendSystemMessage(literalText("This archive already exists!") { color = 0xEE042A })
                    return@runs
                }
                settings.items[archive] = mutableListOf()
                settings.currentArchive = archive
                saveConfig()
                itemIds[archive] = mutableListOf()
                source.player.sendSystemMessage(literalText("The archive $archive was created successfully!") {
                    color = 0x1A631F
                })
            }
        }

        literal("remove") {
            runs {
                val archive = archiveArg()
                if (!archiveCheck(archive)) return@runs
                settings.items.remove(archive)
                itemIds.remove(archive)
                saveConfig()
                if (settings.currentArchive == archive) settings.currentArchive = null
                source.player.sendSystemMessage(literalText("The archive $archive was deleted successfully!") {
                    color = 0x1A631F
                })
            }
        }

        literal("toggle") {
            runs {
                val archive = archiveArg()
                if (!archiveCheck(archive)) return@runs
                if (settings.currentArchive == archive) {
                    source.player.sendSystemMessage(literalText("You already have enabled this archive!") {
                        color = 0x1A631F
                    })
                    return@runs
                }
                val oldArchive = settings.currentArchive
                settings.currentArchive = archive
                saveConfig()
                source.player.sendSystemMessage(literalText("You enabled $archive${if (oldArchive != null) " and disabled $oldArchive" else ""}!") {
                    color = 0x1A631F0
                })
            }
        }
    }
}

private fun CommandContext<ClientCommandSourceStack>.archiveCheck(archiveArg: String): Boolean {
    if (!settings.items.containsKey(archiveArg)) {
        source.player.sendSystemMessage(literalText("You don't have an archive with this name!") {
            color = 0xEE042A
        })
        return false
    }
    return true
}