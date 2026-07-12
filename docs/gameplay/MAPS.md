# Maps and Random Map Resolution

## Active map categories

The active random groups are:

```text
dungeons
misc
research_outposts
```

For release 1.0.0, the `dungeons` group contains only Sky Pillar. Crystal Caves is intentionally disabled.

## Functional map creation

Functional maps are produced by a loot table that starts with:

```json
{
  "type": "minecraft:item",
  "name": "minecraft:map"
}
```

and applies:

```json
{
  "function": "minecraft:exploration_map",
  "destination": "pp_legendarydungeons:some_structure_tag",
  "decoration": "target_x",
  "zoom": 2,
  "skip_existing_chunks": false
}
```

Minecraft converts the blank map into a filled map with saved map data.

## Item-gimmick metadata

Dedicated target:

```json
"minecraft:custom_data": {
  "pp_gimmick": "map_lore",
  "pp_map_target": "skypillar",
  "pp_coords_revealed": false
}
```

Random group:

```json
"minecraft:custom_data": {
  "pp_gimmick": "random_map_lore",
  "pp_random_map_group": "dungeons",
  "pp_coords_revealed": false,
  "pp_random_map_resolved": true,
  "pp_random_map_resolved_target": "skypillar"
}
```

After the item reaches a player inventory, the item-gimmick ticker adds descriptions and approximate coordinates once, then sets `pp_coords_revealed`.

## Current Random Legendary Dungeon Map

For 1.0.0:

```text
Display category: Random Legendary Dungeon Map
Resolved target: Sky Pillar
Destination tag: pp_legendarydungeons:skypillar_tag
```

The generic category name remains useful even though only one dungeon is active. A future release can add another weighted map entry and another `RandomMapTarget`.

## Broad-tag maps

The misc and research-outpost maps use broader structure tags. Minecraft finds a qualifying target, and the resolver compares the filled map’s center with the targets listed in `RandomMapRegistry`.

If no specific match is found, the map still receives category lore and coordinates.

## Crystal Caves release state

Disabled for 1.0.0:

```text
worldgen structure registration
structure set
structure tag
RandomMapRegistry target
LegendaryMapTarget entry
WanderingTraderMapOffer entry
active random-dungeon loot entry
legacy voucher-map route
```

Allowed to remain dormant:

```text
template pools
NBT structure pieces
rubble functions
crystal-heart functions
unused development assets
```

Do not list dormant Crystal Caves content as a player-accessible 1.0.0 feature.
