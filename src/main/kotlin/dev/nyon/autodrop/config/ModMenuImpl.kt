package dev.nyon.autodrop.config

/*? if fabric {*/
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.nyon.autodrop.config.screen.createYaclScreen

@Suppress("unused")
class ModMenuImpl : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory {
            createYaclScreen(it)
        }
    }
}
/*?}*/