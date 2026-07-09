# Map Generation and Random Map System

This document explains how generated map loot tables work in Professor Porker's Legendary Dungeons.

## Functional explorer maps

Functional maps are generated through the vanilla loot-table function:

```json
{
  "function": "minecraft:exploration_map",
  "destination": "pp_legendarydungeons:some_structure_tag",
  "decoration": "target_x",
  "zoom": 2,
  "skip_existing_chunks": false
}
```

Important detail:

- The loot table starts with `"name": "minecraft:map"`.
- The `minecraft:exploration_map` function turns it into a real `minecraft:filled_map`.
- The generated filled map contains saved map data and points toward the selected structure.

## Dedicated map loot tables

Dedicated maps point to one structure tag.

Example:

```json
"destination": "pp_legendarydungeons:skypillar_tag"
```

This is good for maps like:

```text
Sky Pillar Explorer Map
Crystal Caves Locator Map
Trial Chambers Exploration Map
Ancient City Locator Map
```

Dedicated maps should use:

```json
"minecraft:custom_data": {
  "pp_gimmick": "map_lore",
  "pp_map_target": "skypillar",
  "pp_coords_revealed": false
}
```

The `pp_map_target` value should match an entry in:

```text
src/main/java/porker/pp_legendarydungeons/items/maps/MapLoreRegistry.java
```

## Broad random maps

Broad random maps are maps where a category is selected instead of one fixed target.

Examples:

```text
Random Legendary Dungeon Map
Random Strange Structure Map
Random Abandoned Research Outpost Map
```

These use:

```json
"minecraft:custom_data": {
  "pp_gimmick": "random_map_lore",
  "pp_random_map_group": "dungeons",
  "pp_coords_revealed": false,
  "pp_random_map_resolved": false
}
```

The group value must match an entry in:

```text
src/main/java/porker/pp_legendarydungeons/items/maps_findrandom/RandomMapRegistry.java
```

Current groups:

```text
dungeons
misc
research_outposts
```

## Important random-map behavior

A structure tag with multiple structures is not the same as a true random choice.

For example, this broad destination:

```json
"destination": "pp_legendarydungeons:random/dungeons"
```

may keep selecting the nearest or easiest structure to locate, rather than evenly choosing between every structure in the tag.

This is why main dungeon maps use a different approach.

## Random dungeon maps

Main dungeons should randomly choose the dungeon type first, then generate a map to the nearest structure of that selected type.

Current dungeon map logic:

```text
50% Sky Pillar
50% Crystal Caves
```

The random dungeon loot table has multiple entries:

```text
Entry 1:
destination = pp_legendarydungeons:skypillar_tag
pp_random_map_resolved_target = skypillar

Entry 2:
destination = pp_legendarydungeons:crystal_caves_tag
pp_random_map_resolved_target = crystal_caves
```

This is better than pointing the map to `#random/dungeons`, because it prevents a more common or closer dungeon from always overriding the other dungeon type.

## Misc and research random maps

The misc and research outpost random maps can remain broad-tag maps for now.

Current broad tags include:

```text
pp_legendarydungeons:random/misc
pp_legendarydungeons:random/research_outposts
```

These maps allow Minecraft to choose a structure from the tag. The `RandomMapLoreGimmick` then tries to identify the specific target by comparing the generated map center to the possible structures listed in `RandomMapRegistry`.

This is acceptable for smaller structure groups where exact distribution is less important.

## Structure tags

Structure tags live in:

```text
src/main/resources/data/pp_legendarydungeons/tags/worldgen/structure/
```

Examples:

```text
skypillar_tag.json
crystal_caves_tag.json
trial_chamber.json
random/dungeons.json
random/misc.json
random/research_outposts.json
fix_multi/001_torterra_variants.json
fix_multi/004_magicarpjump.json
```

A structure tag can include other structure tags:

```json
{
  "replace": false,
  "values": [
    "#pp_legendarydungeons:skypillar_tag",
    "#pp_legendarydungeons:crystal_caves_tag"
  ]
}
```

Optional entries can use:

```json
{
  "id": "some_mod:some_structure",
  "required": false
}
```

This is useful for compatibility with optional mods.

## Version compatibility

For older Cobblemon versions, remove map entries that point to structures using blocks or structures unavailable in that version.

For example, if Crystal Caves uses Cobblemon 1.8 blocks, then the older Cobblemon 1.6.1-1.7.3 version of the random dungeon loot table should omit the Crystal Caves entry entirely.

For loot tables, this is safer than relying on an empty or missing structure tag.
