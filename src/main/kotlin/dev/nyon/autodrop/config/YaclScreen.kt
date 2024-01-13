package dev.nyon.autodrop.config

import dev.isxander.yacl3.api.*
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder
import dev.isxander.yacl3.api.controller.ItemControllerBuilder
import dev.isxander.yacl3.api.controller.LongFieldControllerBuilder
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder
import dev.nyon.autodrop.config.models.Archive
import dev.nyon.konfig.config.saveConfig
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items

/**
 * @author btwonion
 * @since 19/12/2023
 */
fun createYaclScreen(parent: Screen? = null): Screen {
    val builder = YetAnotherConfigLib.createBuilder()
    builder.title(Component.translatable("menu.autodrop.name"))
    builder.category(
        ConfigCategory.createBuilder().name(Component.translatable("menu.autodrop.archives.title"))
            .appendCreateArchiveOption().appendArchivesOptions().build()
    )
    builder.category(
        ConfigCategory.createBuilder().name(Component.translatable("menu.autodrop.lockedslots.title"))
            .appendLockedSlotsOptions().build()
    )

    builder.category(
        ConfigCategory.createBuilder().name(Component.translatable("menu.autodrop.general.title"))
            .appendGeneralConfigOptions().appendActiveArchivesOptionGroup().build()
    )

    builder.save { saveConfig(settings) }
    val screen = builder.build()
    return screen.generateScreen(parent)
}

private fun ConfigCategory.Builder.appendGeneralConfigOptions(): ConfigCategory.Builder {
    option(
        Option.createBuilder<Boolean>().name(Component.translatable("menu.autodrop.general.active.title"))
            .description(OptionDescription.of(Component.translatable("menu.autodrop.general.active.description")))
            .binding(true, { settings.enabled }, { settings.enabled = it }).controller(TickBoxControllerBuilder::create)
            .build()
    )
    option(
        Option.createBuilder<Long>().name(Component.translatable("menu.autodrop.general.dropdelay.title"))
            .description(OptionDescription.of(Component.translatable("menu.autodrop.general.dropdelay.description")))
            .binding(200, { settings.dropDelay }, { settings.dropDelay = it })
            .controller(LongFieldControllerBuilder::create).build()
    )

    return this
}

private fun ConfigCategory.Builder.appendActiveArchivesOptionGroup(): ConfigCategory.Builder {
    val group = OptionGroup.createBuilder().name(Component.translatable("menu.autodrop.general.enabledarchives.title"))
        .description(OptionDescription.of(Component.translatable("menu.autodrop.general.enabledarchives.description")))

    settings.archives.forEach { archive ->
        group.option(
            Option.createBuilder<Boolean>().name(Component.translatable(archive.name))
                .description(OptionDescription.of(Component.translatable("menu.autodrop.general.enabledarchives.tickbox")))
                .binding(true, { settings.activeArchives.contains(archive.name) }, {
                    if (it) settings.activeArchives.add(archive.name) else settings.activeArchives.removeIf { archiveName -> archiveName == archive.name }
                    reloadArchiveProperties()
                }).controller(TickBoxControllerBuilder::create).build()
        )
    }

    group(group.build())
    return this
}

private fun ConfigCategory.Builder.appendCreateArchiveOption(): ConfigCategory.Builder {
    option(
        ButtonOption.createBuilder().name(Component.translatable("menu.autodrop.archives.createarchive.title"))
            .description(
                OptionDescription.of(
                    Component.translatable("menu.autodrop.archives.createarchive.description"),
                    Component.translatable("menu.autodrop.archives.createarchive.note")
                )
            ).action { _, _ ->
                settings.archives.add(
                    Archive(
                        "Archive ${settings.archives.size + 1}", mutableListOf(), mutableListOf()
                    )
                )
                saveConfig(settings)
            }.build()
    )
    return this
}

private fun ConfigCategory.Builder.appendArchivesOptions(): ConfigCategory.Builder {
    settings.archives.map { it.name }.forEach { archiveName ->
        group(
            ListOption.createBuilder<Item>().name(Component.literal(archiveName))
                .description(OptionDescription.of(Component.translatable("menu.autodrop.archives.edit.description")))
                .binding(mutableListOf(),
                    { settings.archives.first { it.name == archiveName }.items.map { BuiltInRegistries.ITEM.get(it) } },
                    {
                        settings.archives.first { archive -> archive.name == archiveName }.items =
                            it.map { item -> BuiltInRegistries.ITEM.getKey(item) }.toMutableList()
                        reloadArchiveProperties()
                    }).controller(ItemControllerBuilder::create).initial(Items.STONE).collapsed(true).build()
        )
    }
    return this
}

private fun ConfigCategory.Builder.appendLockedSlotsOptions(): ConfigCategory.Builder {
    settings.archives.map { it.name }.forEach { archiveName ->
        group(
            ListOption.createBuilder<Int>().name(Component.literal(archiveName)).description(
                OptionDescription.createBuilder().text(Component.translatable("menu.autodrop.lockedslots.description"))
                    .image(ResourceLocation("autodrop", "image/inventory-slots.png"), 352, 331).build()
            ).binding(mutableListOf(), { settings.archives.first { it.name == archiveName }.lockedSlots }, {
                settings.archives.first { archive -> archive.name == archiveName }.lockedSlots = it.toMutableList()
                reloadArchiveProperties()
            }).controller(IntegerFieldControllerBuilder::create).initial(0).collapsed(true).build()
        )
    }
    return this
}