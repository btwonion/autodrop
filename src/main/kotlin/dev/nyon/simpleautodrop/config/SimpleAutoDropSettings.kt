package dev.nyon.simpleautodrop.config

import com.mojang.brigadier.context.CommandContext
import dev.nyon.simpleautodrop.screen.ConfigScreen
import dev.nyon.simpleautodrop.util.ItemSerializer
import kotlinx.serialization.Serializable
import net.minecraft.client.Minecraft
import net.minecraft.commands.arguments.item.ItemInput
import net.minecraft.world.item.Item
import net.silkmc.silk.commands.ClientCommandSourceStack
import net.silkmc.silk.commands.clientCommand
import net.silkmc.silk.commands.player
import net.silkmc.silk.core.text.literalText

var settings: SimpleAutoDropSettings = SimpleAutoDropSettings(true, hashMapOf(), mutableListOf())
var itemIds = mutableListOf<Int>()

fun reloadCachedIds() {
    itemIds.clear()
    settings.currentArchives.forEach { archive -> settings.items[archive]?.forEach { itemIds += Item.getId(it) } }
}

@Serializable
data class SimpleAutoDropSettings(
    var enabled: Boolean,
    var items: HashMap<String, MutableList<@Serializable(with = ItemSerializer::class) Item>>,
    var currentArchives: MutableList<String>
)

val autoDropCommand = clientCommand("autodrop") {
    literal("list") {
        runs {
            if (settings.items.isEmpty()) {
                source.player.sendSystemMessage(literalText("You don't have any archives yet!") {
                    color = 0x50F9FB
                })
                return@runs
            }
            source.player.sendSystemMessage(literalText("You have the following archives:") {
                color = 0x869097
                emptyLine()
                settings.items.keys.forEach {
                    text("- $it") {
                        color = 0x50F9FB
                    }
                    newLine()
                }
            })
        }
    }

    argument<String>("archive") { archiveArg ->
        suggestList { settings.items.keys }
        literal("add") {
            argument<ItemInput>("item") { itemInput ->
                runs {
                    val archive = archiveArg()
                    if (!archiveCheck(archive)) return@runs
                    val item = itemInput().item
                    if (settings.items[archive]?.contains(item) == true) {
                        source.player.sendSystemMessage(literalText("This item is already enabled for this archive!") {
                            color = 0xEE042A
                        })
                        return@runs
                    }
                    settings.items[archive]?.add(item)
                    saveConfig()
                    reloadCachedIds()
                    source.player.sendSystemMessage(literalText("This item was enabled for this archive!") {
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
                        source.player.sendSystemMessage(literalText("This item is already disabled for this archive!") {
                            color = 0xEE042A
                        })
                        return@runs
                    }
                    settings.items[archive]?.remove(item)
                    saveConfig()
                    reloadCachedIds()
                    source.player.sendSystemMessage(literalText("This item was disabled for this archive!") {
                        color = 0x1A631F
                    })
                }
            }
        }

        literal("list") {
            runs {
                val archive = archiveArg()
                if (!archiveCheck(archive)) return@runs
                if (settings.items[archive]?.isEmpty() == true) {
                    source.player.sendSystemMessage(literalText("You don't have any items in this archive yet!") {
                        color = 0x50F9FB
                    })
                    return@runs
                }
                source.player.sendSystemMessage(literalText("You have the following items enabled for this archive:") {
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
                settings.currentArchives += archive
                saveConfig()
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
                saveConfig()
                if (settings.currentArchives.contains(archive)) settings.currentArchives -= archive
                source.player.sendSystemMessage(literalText("The archive $archive was deleted successfully!") {
                    color = 0x1A631F
                })
            }
        }

        literal("toggle") {
            runs {
                val archive = archiveArg()
                if (!archiveCheck(archive)) return@runs
                if (settings.currentArchives.contains(archive)) {
                    source.player.sendSystemMessage(literalText("You disabled this archive!") {
                        color = 0x1A631F
                    })
                    settings.currentArchives -= archive
                    reloadCachedIds()
                    return@runs
                }
                val oldArchive = settings.currentArchives
                settings.currentArchives += archive
                saveConfig()
                reloadCachedIds()
                source.player.sendSystemMessage(literalText("You enabled $archive${" and disabled $oldArchive"}!") {
                    color = 0x1A631F0
                })
            }
        }
    }
    runs {
        Minecraft.getInstance().setScreen(ConfigScreen(null))
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