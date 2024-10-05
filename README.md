# autodrop

This mod provides a function to automatically drop items from the
inventory if they are blacklisted. You can easily disable it by pressing a hotkey (`default: j`).

## Managing items

![Config screen sample](https://raw.githubusercontent.com/btwonion/autodrop/refs/heads/main/media/config-screen.gif)

- press the keybinding for the config GUI (`default: o`)
- use the configuration section of the modmenu integration
- archives can be toggled by double-clicking the archive in the GUI

### Component/Nbt filtering

*You have to be careful: Setting the item to air and leaving an empty component will cause the mod to drop every item.*

The format of the components/nbt is always the item command syntax of the version of Minecraft you're playing on.
Unless you know the syntax by heart, you can simply use
a [give command generator](https://www.gamergeeks.net/apps/minecraft/give-command-generator) and copy the component
part (enclosed by `{}` or `[]`).

### Other

If you need help with any of my mods just join my [discord server](https://nyon.dev/discord).