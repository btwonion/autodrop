# autodrop

This mod provides a function to automatically drop items from the inventory.

https://github.com/user-attachments/assets/470a3d66-1e79-49e9-88df-522e7640d1c1


## Managing profiles

- press the keybinding for the config GUI (`default: o`)
- use the configuration section of the modmenu integration (Fabric) or the mods list (NeoForge)

Items are organized in profiles. Each profile can be turned on or off in the profile list, and every enabled profile
applies at the same time. New profiles start turned off.

A profile works in one of two modes:

- **Drop listed items:** the listed items are dropped.
- **Keep listed items:** the listed items are kept and every other item is dropped. You have to review what would be
  dropped before a keep list can be turned on. If several keep lists are on, an item on any of them is kept.

Each profile has three tabs:

- **Items:** add items with the item picker (several at once) and click an item to edit its rule.
- **Protected slots:** items in protected slots are never dropped by any enabled profile. New profiles protect the
  armor slots.
- **Preview:** shows what would be dropped right now and why each slot is dropped or kept.

Changes are only applied when you press *Save changes*. Dropping is paused while the screen is open. You can edit
profiles outside a world as well, but the inventory view and the preview need one.

### Item rules

Every item in a drop list can be configured:

- **Amount:** drop all matching items, half of each stack, or exactly X items per trigger (taken from several stacks
  if needed).
- **Minimum in inventory:** only drop once you carry at least this many matching items in total.

### Triggers

- picking up items (on by default)
- starting to crouch (off by default)
- the trigger keybinding (`default: k`)

The toggle keybinding (`default: j`) turns AutoDrop on or off. The delay between a trigger and dropping can be changed
in the general settings.

### Component/Nbt filtering

The item data filter can be found in the advanced options of an item rule.

*You have to be careful: Using "Any item" (`*`) and leaving the filter empty will cause the mod to drop every item that
is not protected or kept.*

The format of the filter is always the item predicate syntax of the version of Minecraft you're playing on.
Unless you know the syntax by heart, you can use
a [give command generator](https://www.gamergeeks.net/apps/minecraft/give-command-generator)
and copy the component part (enclosed by `[]`).
The syntax is also described on
this [wiki post](https://minecraft.wiki/w/Argument_types#item_predicate). 

For sub-types of items, such as Bottle of Water (subtype of Potion) use a specialized relevant generator like the [Minecraft Maps Generators](https://www.minecraftmaps.com/tools/give/potions).

<br>

**How to drop everything except a specific item?** 

Create a profile in *Keep listed items* mode and add the items you want to keep.

For finer control, use "Any item" with a filter like `[!minecraft:stored_enchantments={mending:1}]`. It will drop
every item that does not have Mending enchanted. If you want to only drop enchanted books that do not have Mending
enchanted, use `minecraft:enchanted_book` instead of "Any item".

### Other

If you need help with any of my mods, join my [discord server](https://nyon.dev/discord).
