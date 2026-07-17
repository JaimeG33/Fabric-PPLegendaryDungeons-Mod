# Datapack Map Metadata

Functional-map descriptions and random-map resolution metadata are loaded from
server-data resources under:

```text
data/<namespace>/wtrader/map_targets/
data/<namespace>/wtrader/map_groups/
```

The folders are scanned recursively and reload with `/reload`.

## Map target

A target supplies the reusable destination name and description used by both a
dedicated map and a random map that resolves to the same structure.

```json
{
  "id": "pp_legendarydungeons:ancient_battles/gvk/island_1",
  "display_name": "Ancient Battle Island",
  "description_lines": [
    {
      "text": "A remote island tied to an ancient clash between powerful forces of land and sea.",
      "color": "gray",
      "italic": false
    }
  ],
  "structure_tag": "pp_legendarydungeons:random/specific/ancient_battle_gvk_island_1",
  "groups": [
    "pp_legendarydungeons:misc"
  ],
  "aliases": [
    "ancient_battle_gvk_island_1"
  ]
}
```

Fields:

- `id`: Stable metadata ID. Built-in entries normally use the concrete
  worldgen structure ID, but this is a naming convention rather than a loader
  requirement.
- `display_name`: Destination heading shown when a random map identifies the
  target.
- `description_lines`: Reusable lore lines. Supported optional styling fields
  are `color`, `bold`, and `italic`.
- `structure_tag`: Specific structure tag used by random-map resolution.
  Required when `groups` is not empty.
- `groups`: Random-map group IDs this target joins. This is additive across
  datapacks; an addon can join an existing group without replacing its file.
- `aliases`: Optional legacy IDs or shorthand values accepted from existing map
  item custom data.

The actual map item name remains in the map loot table's `set_name` function.

## Map group

```json
{
  "id": "pp_legendarydungeons:misc",
  "display_name": "Random Strange Structure",
  "description_lines": [
    {
      "text": "This map points toward one of the smaller strange structures.",
      "color": "gray",
      "italic": false
    }
  ],
  "aliases": [
    "misc"
  ]
}
```

Targets join a group from their own `groups` array. Group files intentionally
do not contain a complete target list, preventing addon datapacks from needing
to replace a built-in group merely to add one destination.

## Functional-map custom data

Dedicated map:

```json
"minecraft:custom_data": {
  "pp_gimmick": "map_lore",
  "pp_map_target": "pp_legendarydungeons:ancient_battles/gvk/island_1",
  "pp_coords_revealed": false
}
```

Random map:

```json
"minecraft:custom_data": {
  "pp_gimmick": "random_map_lore",
  "pp_random_map_group": "pp_legendarydungeons:misc",
  "pp_coords_revealed": false,
  "pp_random_map_resolved": false
}
```

Legacy bare IDs remain supported through the built-in `aliases` arrays.

## Random-map retries

An unresolved map receives up to three attempts:

1. Normal 32-chunk locate radius.
2. Six seconds later, a one-time saturated doubling to 64 chunks.
3. Six seconds later, the same doubled radius; it is not doubled again.

The saturating doubling returns `Integer.MAX_VALUE` instead of overflowing if a
future base radius is ever greater than half the integer maximum. Pending state
is stored on the item so moving slots, logging out, or restarting does not cause
rapid repeated locate calls.

After the final failure, the map receives the generic group description,
available coordinates, and `Specific destination could not be identified.`
