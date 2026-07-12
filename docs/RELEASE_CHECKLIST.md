# Release Checklist — 1.0.0

## Release blockers found in the current repository

### 1. Remove the disabled Crystal Caves branch from the active random-dungeon loot table

The worldgen structure, structure set, structure tag, Java map registry entry, and legacy enum entries were removed correctly. However, this active file still contains a second map entry pointing to the deleted Crystal Caves tag:

```text
src/main/resources/data/pp_legendarydungeons/loot_table/maps/random/find_random_dungeon.json
```

Replace it with:

```json
{
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "weight": 1,
          "name": "minecraft:map",
          "functions": [
            {
              "function": "minecraft:exploration_map",
              "destination": "pp_legendarydungeons:skypillar_tag",
              "decoration": "target_x",
              "zoom": 2,
              "skip_existing_chunks": false
            },
            {
              "function": "minecraft:set_name",
              "name": {
                "text": "Random Legendary Dungeon Map"
              }
            },
            {
              "function": "minecraft:set_components",
              "components": {
                "minecraft:rarity": "rare",
                "minecraft:custom_data": {
                  "pp_gimmick": "random_map_lore",
                  "pp_random_map_group": "dungeons",
                  "pp_coords_revealed": false,
                  "pp_random_map_resolved": true,
                  "pp_random_map_resolved_target": "skypillar"
                }
              }
            }
          ]
        }
      ]
    }
  ]
}
```

Until another legendary dungeon is enabled, the “Random Legendary Dungeon Map” resolves to Sky Pillar every time. Keeping the generic name preserves the data model for future expansion.

### 2. Disable the retired voucher-map tick path

The following tick function is still active:

```text
pp_legendarydungeons:other/maps/voucher_check
```

It scans players every tick, calls `other/maps/find_map`, and still contains the Crystal Caves custom-model-data branch.

Edit:

```text
src/main/resources/data/pp_legendarydungeons/tags/function/tick.json
```

Recommended release version:

```json
{
  "values": [
    "pp_legendarydungeons:rt/event_start_rayquaza",
    "pp_legendarydungeons:rt/event_secret_check",
    "pp_legendarydungeons:other/bannedblocks",
    "pp_legendarydungeons:other/dungeon/which_vault",
    "pp_legendarydungeons:other/dungeon/mc_enemies"
  ]
}
```

After removing the tick entry, these legacy files can be deleted or moved outside `src/main/resources`:

```text
src/main/resources/data/pp_legendarydungeons/function/other/maps/voucher_check.mcfunction
src/main/resources/data/pp_legendarydungeons/function/other/maps/find_map.mcfunction
src/main/resources/data/pp_legendarydungeons/function/other/maps/load/crystal_caves.mcfunction
src/main/resources/data/pp_legendarydungeons/loot_table/maps/find_crystal_caves.json
```

The dormant Crystal Caves template pools, NBT pieces, rubble functions, and crystal-heart functions can remain. Without active worldgen registration or an active caller, they do not generate the dungeon.

## Metadata decisions

Recommended internal release version:

```kotlin
version = "1.0.0"
```

Recommended public author metadata:

```json
"authors": [
  "Professor Porker"
]
```

Recommended release file name after the build:

```text
cobblemon-eld-1.0.0-mc1.21.1-cob1.6.1-1.7.3.jar
```

Renaming the completed remapped JAR does not change the Fabric mod ID, entrypoints, internal version, or runtime behavior.

## Release-polish items

### Pokémon loot logging

`PokemonLootTableRegistrar` still contains diagnostic-oriented comments and INFO logs. The feature is enabled, so update the old comment above:

```java
private static final boolean EXECUTE_LOOT_TABLES = true;
```

Recommended wording:

```java
/*
 * Master switch for the Cobblemon-to-Minecraft loot-table bridge.
 * Keep enabled in release builds.
 */
```

Consider changing the per-defeat diagnostic messages from `LOGGER.info` to `LOGGER.debug`. Keep warnings and errors at their current levels.

### Dependency metadata

The current metadata is functional, but release metadata can be more explicit:

```json
"depends": {
  "fabricloader": ">=0.17.2",
  "minecraft": "1.21.1",
  "java": ">=21",
  "fabric-api": ">=0.116.6+1.21.1",
  "fabric-language-kotlin": ">=1.13.6+kotlin.2.2.20",
  "cobblemon": ">=1.6.1 <1.8.0"
}
```

The project directly uses Kotlin `Unit` in Java event subscriptions, so declaring Fabric Language Kotlin is clearer even though Cobblemon also brings it into normal installations.

## Build and artifact checks

Run:

```powershell
.\gradlew.bat clean build
```

Use the remapped JAR from:

```text
build/libs/
```

Do not upload the `-sources.jar`.

Open the finished JAR with 7-Zip and verify:

```text
fabric.mod.json
assets/pp_legendarydungeons/icon.png
pp_legendarydungeons.mixins.json
data/pp_legendarydungeons/
```

Confirm the processed `fabric.mod.json` contains:

```json
"version": "1.0.0"
```

## Fresh-world checks

- `/locate structure pp_legendarydungeons:dungeon/crystal_caves` does not resolve.
- A newly generated Random Legendary Dungeon Map points to Sky Pillar.
- No log errors mention `crystal_caves_tag`.
- Sky Pillar generates and can be located.
- Rayquaza normal and secret summons still work.
- Ancient Key opens the intended configured vault.
- Cartographer novice and master injected trades work.
- A natural wandering trader can remain vanilla or become a custom profile.
- Klefki battle defeat and direct player kill can roll the injected table.
- Warden, Elder Guardian, and Wither retain normal loot and can roll the key injection.
- Cartographer and igloo/taiga chest injections retain vanilla loot.

## Two-version smoke test

Repeat the core checks in two separate instances:

1. Cobblemon 1.6.1 + Minecraft 1.21.1
2. Cobblemon 1.7.3 + Minecraft 1.21.1

Do not reuse one instance’s Cobblemon configuration or world folder for the other test.
