# Paper Game (Fabric 1.21)

A separate, client-side Fabric mod reconstructed from the compiled BetterTOTEM JAR. It provides a configurable number-to-item map and an on-screen reference panel. Press **O** to toggle the panel.

## Build on GitHub
1. Create a new GitHub repository and upload all files in this folder.
2. In GitHub, open **Actions**. The included workflow builds the mod automatically.
3. Download the `paper-game-mod` artifact from a successful workflow run.
4. Put the built JAR in your Minecraft `mods` folder with Fabric Loader and Fabric API for Minecraft 1.21.

You need Java 21 to build locally: `./gradlew build` (Windows: `gradlew.bat build`).

## Choose the items for numbers
Run the game once. Edit `.minecraft/config/papergame.json`. Each value must be a Minecraft item ID such as `minecraft:paper`, `minecraft:diamond`, or `minecraft:oak_log`:

```json
{
  "hudEnabled": true,
  "numbers": {
    "1": "minecraft:paper",
    "2": "minecraft:map",
    "3": "minecraft:book",
    "4": "minecraft:feather",
    "5": "minecraft:amethyst_shard",
    "6": "minecraft:prismarine_shard",
    "7": "minecraft:gold_nugget",
    "8": "minecraft:iron_nugget",
    "9": "minecraft:emerald"
  }
}
```

Restart Minecraft after editing the file. Each number should use a different item ID for unambiguous mapping.

## Important limitation
The uploaded JAR contains compiled classes only. This project is a clean standalone reconstruction of the configurable item-number mapping and HUD reference, **not an exact extraction** of BetterTOTEM's original Paper Game. It does not yet reproduce the original dispenser-slot disguising, hotkey rigging, or all original overlay behavior. The public `getNumberForStack(ItemStack)` helper is provided for implementing those mechanics in later code.
