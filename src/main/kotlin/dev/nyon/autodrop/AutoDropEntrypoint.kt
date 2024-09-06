package dev.nyon.autodrop

import dev.nyon.autodrop.config.Config
import dev.nyon.autodrop.config.config
import dev.nyon.autodrop.config.migrate
import dev.nyon.autodrop.config.reloadArchiveProperties
import dev.nyon.autodrop.extensions.DataComponentPatchSerializer
import dev.nyon.autodrop.extensions.ItemSerializer
import dev.nyon.autodrop.extensions.StoredComponents
import dev.nyon.konfig.config.config
import dev.nyon.konfig.config.loadConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.client.Minecraft
import net.minecraft.world.item.Item
import java.nio.file.Path

/*? if fabric {*/
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.loader.api.FabricLoader

object AutoDropEntrypoint : ClientModInitializer {
    override fun onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(AutoDrop.menuKeyBind)
        KeyBindingHelper.registerKeyBinding(AutoDrop.toggleKeyBind)
        initialize(FabricLoader.getInstance().configDir.resolve("autodrop.json"))
    }
}

/*?} else {*/
/*import dev.nyon.autodrop.config.screen.createYaclScreen
import net.neoforged.fml.ModLoadingContext
import net.neoforged.fml.common.Mod
import net.neoforged.fml.loading.FMLLoader
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

typealias CSF = /^? if <1.20.6 {^/ /^net.neoforged.neoforge.client.ConfigScreenHandler.ConfigScreenFactory ^//^?} else {^/ net.neoforged.neoforge.client.gui.IConfigScreenFactory /^?}^/

@Mod("autodrop")
object AutoDropEntrypoint {
    init {
        initialize(FMLLoader.getGamePath().resolve("config/autodrop.json"))

        MOD_BUS.addListener<RegisterKeyMappingsEvent> {
            it.register(AutoDrop.menuKeyBind)
            it.register(AutoDrop.toggleKeyBind)
        }

        ModLoadingContext.get().registerExtensionPoint(CSF::class.java) {
            CSF { _, parent -> createYaclScreen(parent) }
        }
    }
}
*//*?}*/

private fun initialize(configDir: Path) {
    minecraft = Minecraft.getInstance()
    mcScope = CoroutineScope(SupervisorJob() + minecraft.asCoroutineDispatcher())

    config(configDir, 1, Config(), jsonBuilder = {
        serializersModule = SerializersModule {
            contextual(Item::class, ItemSerializer)
            contextual(StoredComponents::class, DataComponentPatchSerializer)
        }
    }) { jsonTree, version -> migrate(jsonTree, version) }
    config = loadConfig<Config>()

    reloadArchiveProperties()
}