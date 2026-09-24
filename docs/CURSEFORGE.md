# CurseForge release checklist

## Project page

- **Name:** Minecraft Random Button
- **Summary:** Press a button and let chance take over: face chaotic events, discover themed loot, or charge your odds with Lucky and Unlucky Cubes.
- **Minecraft version:** 1.21.1
- **Categories:** Adventure and RPG; choose any additional categories only if they fit CurseForge's available options and the mod.
- **License:** MIT
- **Environment:** Both client and server. Fabric requires Fabric API.
- **Project status:** Beta

### Description

Press the Random Button and trigger an unpredictable event in your Minecraft world. Get themed loot and positive effects, or deal with hostile mobs, TNT, and falling meteors. Use Lucky and Unlucky Cubes to shift the next five eligible events toward positive or negative outcomes. Their charges persist, stack, and appear in a compact HUD.

This is an early beta. Event balance and behavior may change, and bugs are expected. Back up important worlds before trying it. To report an issue, include your Minecraft version, loader and version, steps to reproduce, and relevant log excerpt.

### Features

- Random events involving mobs, effects, weather, time, loot, TNT, and meteors.
- Lucky and Unlucky Cubes that add five-use positive or negative event pools.
- Themed loot events and a recall sigil.
- Operator commands for testing and inspecting events. See the included documentation or project source for details.

## Files to upload

Create separate files for the two loaders so players can choose the correct jar:

- `randombutton-fabric-1.21.1-0.1.0-beta.1.jar` — Fabric, Minecraft 1.21.1; mark Fabric API as a required dependency.
- `randombutton-neoforge-1.21.1-0.1.0-beta.1.jar` — NeoForge, Minecraft 1.21.1.

Set both files to **Beta** and list Minecraft 1.21.1 as the supported game version. Do not mark them as release candidates or stable releases.

### File changelog

First public beta. Adds the Random Button event system, Lucky and Unlucky Cubes, themed loot, meteor events, and a recall sigil. Supports Minecraft 1.21.1 on Fabric and NeoForge. Fabric requires Fabric API. Expect bugs and balance changes.

## Before publishing

- Add a square project icon and at least one clear in-game screenshot. Use images you have permission to publish.
- Check that the uploaded filenames match the loader and Minecraft version.
- Add project source/repository and issue tracker links if you want players to report bugs there.
- Confirm the CurseForge project license selection says MIT.
- Publish as Beta and verify the project page and both file pages after upload.
