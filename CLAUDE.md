# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

AutoDrop is a client-side Minecraft mod (Kotlin, mixins in Java) that drops unwanted items from the player's
inventory. One source tree targets several Minecraft versions and both loaders (Fabric/Quilt and NeoForge) via
[Stonecutter](https://stonecutter.kikugie.dev/); [modstitch](https://github.com/isXander/modstitch) hides the
Loom/ModDevGradle differences. Targets are declared in `settings.gradle.kts` (`26.1`, `26.2`, `26.3` × fabric/neoforge).

## Commands

Requires JDK 25 (same as CI).

```bash
./gradlew build                          # all targets; what CI runs. Jars land in versions/<target>/build/libs/
./gradlew :26.3-fabric:build             # a single target
./gradlew :26.3-fabric:runClient         # dev client, Fabric
./gradlew :26.3-neoforge:runMainClient   # dev client, NeoForge (game dir: run/)
./gradlew "Set active project to 26.1-fabric"   # switch the version the src/ comments are written for
```

There are no tests and no linter; formatting follows `.editorconfig` (4 spaces, 120 columns).

## Stonecutter

- Version- and loader-specific code lives in comments such as `//? if >26.1`, `/*? if fabric {*/ … /*?} else {*/ … /*?}*/`
  and `client.gui/*? if >26.1 {*/.hud/*?}*/`. Code for inactive versions is commented out in `src/`.
- Each `//? if` directive must stay on its own line, directly above the line it guards. An IDE reformat that moves
  it to the end of the previous code line still builds, but switching the active version then leaves both branches
  uncommented. After touching directives, switch to another target and back and check `git diff` is empty.
- The repo is committed with `26.3-fabric` active (`stonecutter.gradle.kts`); switch back before committing.
- Edit only `src/`; `versions/*/build/generated/stonecutter` is generated.
- Shared mod properties (`mod.major-version`, `mod.beta`, ...) are in `gradle.properties`; per-target Minecraft
  version, version range and dependency versions (`vers.deps.*`, `modstitch.platform`) are in
  `versions/<target>/gradle.properties`.

## Architecture

**Hooks.** The mixins in `src/main/java/dev/nyon/autodrop/mixins` only forward events into Kotlin:
`MinecraftClientMixin` runs `KeyBindings.handleKeybindings` and `AutoDrop.tick()` every client tick,
`InventoryMixin` reports inventory writes, `PlayerMixin` reports the start of crouching, and
`AbstractContainerScreenMixin` fires the keybindings while a container screen has focus.
`AutoDropEntrypoint.kt` (loader-specific via Stonecutter) registers keybindings and loads the config;
`ModMenuImpl` / NeoForge's `IConfigScreenFactory` open `ProfileWorkspaceScreen`.

**Dropping (`AutoDrop.kt`).** The pickup trigger fires only when both an inventory mixin event happened and the
per-tick inventory snapshot changed; `planning/DropTriggerState` absorbs AutoDrop's own predicted result so its
drops never retrigger it. Triggers coalesce into one coroutine on the client thread (`mcScope` dispatches onto
`Minecraft`), which waits `dropDelay`, takes a fresh snapshot, plans and then sends container clicks through
`gameMode.handleContainerInput` (throw a whole stack, right-click + click outside for half, otherwise one throw
per item). Nothing runs while a config screen (`DraftScreen`) is open.

**Planning (`planning/`).** `DropPlanner` is pure Kotlin with no Minecraft imports and is shared by execution and
the config screen's preview. Drop semantics belong there, so the preview never disagrees with what is dropped.
`MinecraftPlanner` adapts the game state: it snapshots only player-owned slots, keyed by InventoryMenu index
(5–8 armor, 9–35 main, 36–44 hotbar, 45 offhand; the same indices `Profile.protectedSlots` stores), remaps them
to the currently open menu by container identity, and compiles item-predicate filters via `PredicateCache`
(server registries in a world, `VanillaRegistries` outside one).

**Config (`config/`).** `Config` → `Profile` (DROP or KEEP mode, protected slots) → `ProfileRule` (item ID or `null`
for any item, predicate, `QuantityMode`, inventory-wide minimum). The live config is the top-level `config` var
in `ConfigHandler.kt`. Konfig stores `{"version", "config"}` and decodes without looking at the version; it calls
`migrate` only when decoding fails and the stored version differs from `CONFIG_VERSION`, and resets to defaults
when decoding fails at the current version. So a format change must bump `CONFIG_VERSION` and add a migration
branch, and unknown keys must keep failing (don't enable `ignoreUnknownKeys`): that is how old `archives` configs
reach `migrateArchives`.

**Screens (`config/screen/`).** Built from vanilla widgets on the 26.x render-state API (`GuiGraphicsExtractor`,
`extract*` methods). Every screen extends `DraftScreen` and edits one shared `ConfigDraft` (a deep copy); only
`ProfileWorkspaceScreen.save()` validates (`validateConfig`), writes with `saveConfig` and replaces `config`.

**Translations.** `assets/autodrop/lang/en_us.json` and `de_de.json` must keep the same keys. Screen strings use
`screenComponent("key")`, which prefixes `menu.autodrop.screen.`.

## Releases

`changelog.md` holds only the notes for the upcoming release; publishing and the Discord post read it.
Releases run from the manually dispatched `release.yml` workflow (`./gradlew releaseMod`). `README.md` is synced
to the Modrinth and CurseForge descriptions on pushes to `main`.
