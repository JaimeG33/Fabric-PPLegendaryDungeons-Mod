# Map Lore, Coordinates, and Structure Descriptions

This document explains how maps receive descriptions, coordinates, and specific destination names after entering the player's inventory.

## Why lore is added after purchase/loot

Generated explorer maps can be sold by traders, given by loot tables, or obtained from archaeology/dig rewards.

Instead of hardcoding all final lore at the time of creation, the mod uses the item-gimmick system to post-process maps after the player owns them.

This allows the same logic to work for:

```text
Wandering trader maps
Loot table maps
Archaeology maps
Future quest reward maps
Future frozen armor stand maps
```

## Normal map lore

Normal structure maps use:

```text
pp_gimmick = "map_lore"
```

Required custom data:

```json
"minecraft:custom_data": {
  "pp_gimmick": "map_lore",
  "pp_map_target": "skypillar",
  "pp_coords_revealed": false
}
```

The target value must exist in:

```text
src/main/java/porker/pp_legendarydungeons/items/maps/MapLoreRegistry.java
```

Example target ids:

```text
skypillar
crystal_caves
ancient_city
fishing_boats
shipwreck_coves
wishing_weald
mega_site
trial_chambers
```

## Normal map processing flow

`MapLoreGimmick` does this:

1. Check that the item is a filled map.
2. Check `pp_coords_revealed`.
3. Read `pp_map_target`.
4. Find the matching entry in `MapLoreRegistry`.
5. Get coordinates from stored custom data or the map's saved center.
6. Append lore lines and approximate coordinates.
7. Set `pp_coords_revealed = true`.

Once `pp_coords_revealed` is true, the item will not be processed again.

## Coordinates

Coordinates are displayed as an approximate destination:

```text
Approximate destination:
X: ####, Z: ####
```

The coordinate source is checked in this order:

1. `pp_target_x` and `pp_target_z` if already stored.
2. The saved center of the generated filled map.
3. Unknown, if no usable coordinates are available.

The map center is usually close enough for gameplay purposes, but it may not be the exact structure origin block.

## Random map lore

Random maps use:

```text
pp_gimmick = "random_map_lore"
```

Required custom data:

```json
"minecraft:custom_data": {
  "pp_gimmick": "random_map_lore",
  "pp_random_map_group": "dungeons",
  "pp_coords_revealed": false,
  "pp_random_map_resolved": false
}
```

Optional but recommended custom data when the loot table already knows which specific structure was selected:

```json
"pp_random_map_resolved": true,
"pp_random_map_resolved_target": "skypillar"
```

## Random map processing flow

`RandomMapLoreGimmick` does this:

1. Check that the item is a filled map.
2. Check `pp_coords_revealed`.
3. Read `pp_random_map_group`.
4. Find the group in `RandomMapRegistry`.
5. Get coordinates from stored data or the map center.
6. If `pp_random_map_resolved_target` exists, use it directly.
7. Otherwise, ask `RandomMapResolver` to identify the closest matching target.
8. Add broad group lore.
9. Add identified destination lore if a target was resolved.
10. Add approximate coordinates.
11. Set `pp_coords_revealed = true`.

## Random map registry

Random map groups and possible targets are defined in:

```text
src/main/java/porker/pp_legendarydungeons/items/maps_findrandom/RandomMapRegistry.java
```

Each group has:

```text
id
display name
fallback description
list of possible RandomMapTarget entries
```

Each target has:

```text
id
display name
structure tag id
description lines
```

Example:

```java
new RandomMapTarget(
    "skypillar",
    "Sky Pillar",
    "pp_legendarydungeons:skypillar_tag",
    List.of(
        Component.literal("A towering dungeon connected to Rayquaza.")
            .withStyle(ChatFormatting.GRAY)
    )
)
```

## Resolver fallback

If a random map does not already have `pp_random_map_resolved_target`, the resolver compares the map's saved center to the nearest known structures in that map group.

This is useful for broad-tag maps like:

```text
random/misc
random/research_outposts
```

If the resolver cannot identify the specific structure, the map still gets broad category lore and approximate coordinates.

It will show:

```text
Specific destination could not be identified.
```

This is a safe fallback and should not crash the game.

## Best practice

Use normal `map_lore` for maps that always point to one known target.

Use `random_map_lore` for maps that represent a category.

For important categories where you need real randomness, such as main dungeons, make the loot table randomly select between specific map entries and store `pp_random_map_resolved_target`.

For less important categories, such as misc structures and research outposts, broad tags are acceptable.
