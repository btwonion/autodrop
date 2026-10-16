package dev.nyon.autodrop

import dev.nyon.autodrop.config.CONFIG_VERSION
import dev.nyon.autodrop.config.Config
import dev.nyon.autodrop.config.config
import dev.nyon.autodrop.config.migrate
import dev.nyon.konfig.config.config
import dev.nyon.konfig.config.loadConfig
import java.nio.file.Path

/*? if fabric {*/
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.fabricmc.loader.api.FabricLoader

object AutoDropEntrypoint : ClientModInitializer {
    override fun onInitializeClient() {
        KeyBindings.keyBinds.keys.forEach(KeyMappingHelper::registerKeyMapping)
        initialize(FabricLoader.getInstance().configDir.resolve("autodrop.json"))
    }
}

/*?} else {*/
/*import dev.nyon.autodrop.config.screen.ProfileWorkspaceScreen
import dev.nyon.klf.MOD_BUS
import net.neoforged.fml.ModLoadingContext
import net.neoforged.fml.common.Mod
import net.neoforged.fml.loading.FMLLoader
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import net.neoforged.neoforge.client.gui.IConfigScreenFactory

@Mod("autodrop")
object AutoDropEntrypoint {
    init {
        val gamePath = FMLLoader.getCurrent().gameDir
        initialize(gamePath.resolve("config/autodrop.json"))

        MOD_BUS.addListener<RegisterKeyMappingsEvent> {
            KeyBindings.keyBinds.keys.forEach(it::register)
        }

        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory::class.java) {
            IConfigScreenFactory { _, parent -> ProfileWorkspaceScreen(parent) }
        }
    }
}
*//*?}*/

private fun initialize(configDir: Path) {
    config(configDir, CONFIG_VERSION, Config()) { json, jsonTree, version -> migrate(json, jsonTree, version) }
    config = loadConfig<Config>()
}
