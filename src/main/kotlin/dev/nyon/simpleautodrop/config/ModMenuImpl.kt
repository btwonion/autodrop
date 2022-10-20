package dev.nyon.simpleautodrop.config

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import com.terraformersmc.modmenu.gui.ModsScreen
import dev.nyon.simpleautodrop.screen.ConfigScreen

class ModMenuImpl : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory {
            ConfigScreen(ModsScreen(null))
        }
    }
}