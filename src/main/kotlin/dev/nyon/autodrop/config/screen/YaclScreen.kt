package dev.nyon.autodrop.config.screen

import dev.isxander.yacl3.dsl.*
import dev.nyon.autodrop.config.config
import dev.nyon.autodrop.minecraft
import dev.nyon.konfig.config.saveConfig
import net.minecraft.client.gui.screens.Screen

fun createYaclScreen(parent: Screen? = null): Screen = YetAnotherConfigLib("autodrop") {
    val general by categories.registering {
        val enabled by rootOptions.registering {
            binding(true, { config.enabled }, { config.enabled = it })
            controller = tickBox()
            descriptionBuilder {
                addDefaultText(1)
            }
        }

        val screen by rootOptions.registeringButton {
            action { parent, _ ->
                minecraft.setScreen(ArchiveScreen(parent))
            }

            descriptionBuilder {
                addDefaultText(1)
            }
        }

        val delay by rootOptions.registering {
            binding(200, { config.dropDelay }, { config.dropDelay = it })
            controller = numberField(min = 0L)
            descriptionBuilder {
                addDefaultText(1)
            }
        }

        val triggers by groups.registering {
            descriptionBuilder {
                addDefaultText(1)
            }

            val pick by options.registering {
                binding(true, { config.triggerConfig.onPickup }, { config.triggerConfig.onPickup = it })
                controller = tickBox()
                descriptionBuilder {
                    addDefaultText(1)
                }
            }

            val sneak by options.registering {
                binding(false, { config.triggerConfig.onSneak }, { config.triggerConfig.onSneak = it })
                controller = tickBox()
                descriptionBuilder {
                    addDefaultText(1)
                }
            }

            val jump by options.registering {
                binding(false, { config.triggerConfig.onJump }, { config.triggerConfig.onJump = it })
                controller = tickBox()
                descriptionBuilder {
                    addDefaultText(1)
                }
            }

            val switch by options.registering {
                binding(false, { config.triggerConfig.onSlotSwitch }, { config.triggerConfig.onSlotSwitch = it })
                controller = tickBox()
                descriptionBuilder {
                    addDefaultText(1)
                }
            }
        }
    }

    save { saveConfig(config) }
}.generateScreen(parent)