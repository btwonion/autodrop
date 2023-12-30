package dev.nyon.autodrop.config.screen

import dev.isxander.yacl3.api.*
import dev.isxander.yacl3.api.controller.LongFieldControllerBuilder
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder
import dev.nyon.autodrop.config.settings
import dev.nyon.autodrop.minecraft
import dev.nyon.konfig.config.saveConfig
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

/**
 * @author btwonion
 * @since 19/12/2023
 */
fun createYaclScreen(parent: Screen? = null): Screen {
    val builder = YetAnotherConfigLib.createBuilder()
    builder.title(Text.translatable("menu.autodrop.name"))
    builder.category(
        ConfigCategory.createBuilder()
            .name(Text.translatable("menu.autodrop.general.title"))
            .appendItemConfigButton()
            .appendGeneralConfigOptions()
            .build()
    )
    builder.save { saveConfig(settings) }
    val screen = builder.build()
    return screen.generateScreen(parent)
}

private fun ConfigCategory.Builder.appendItemConfigButton(): ConfigCategory.Builder {
    option(
        ButtonOption.createBuilder()
            .name(Text.translatable("menu.autodrop.general.itemconfig.title"))
            .description(OptionDescription.of(Text.translatable("menu.autodrop.general.itemconfig.description")))
            .action { screen, _ -> minecraft.setScreen(ArchivesConfigScreen(screen)) }
            .build()
    )
    return this
}

private fun ConfigCategory.Builder.appendGeneralConfigOptions(): ConfigCategory.Builder {
    option(
        Option.createBuilder<Boolean>()
            .name(Text.translatable("menu.autodrop.general.active.title"))
            .description(OptionDescription.of(Text.translatable("menu.autodrop.general.active.description")))
            .binding(settings.enabled, { settings.enabled }, { settings.enabled = it })
            .controller(TickBoxControllerBuilder::create)
            .build()
    )
    option(
        Option.createBuilder<Long>()
            .name(Text.translatable("menu.autodrop.general.dropdelay.title"))
            .description(OptionDescription.of(Text.translatable("menu.autodrop.general.dropdelay.description")))
            .binding(settings.dropDelay, { settings.dropDelay }, { settings.dropDelay = it })
            .controller(LongFieldControllerBuilder::create)
            .build()
    )
    return this
}