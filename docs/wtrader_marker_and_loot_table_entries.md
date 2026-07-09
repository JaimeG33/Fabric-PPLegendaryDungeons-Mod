# WTrader JSON System Addendum: Marker Tags and Loot Table Trade Entries

This addendum documents the new marker behavior and the new `loot_table` trade-pool entry support.

## Marker Tags

The wandering trader feature now recognizes three marker styles.

### 1. Normal rolled trader

Use:

```text
pp_wtraders
```

This keeps the previous behavior:

```text
20% vanilla wandering trader
40% custom no-map trader
40% custom map trader
```

Example marker:

```mcfunction
summon armor_stand ~ ~ ~ {Tags:["pp_wtraders"],Invisible:1b,Marker:1b}
```

### 2. Roll one profile type

Use:

```text
pp_wtrader_profiletype
```

Then add exactly one profile-type tag:

```text
custom_no_map
custom_map
```

Examples:

```mcfunction
summon armor_stand ~ ~ ~ {Tags:["pp_wtrader_profiletype","custom_no_map"],Invisible:1b,Marker:1b}
summon armor_stand ~ ~ ~ {Tags:["pp_wtrader_profiletype","custom_map"],Invisible:1b,Marker:1b}
```

If the marker is missing `custom_no_map`/`custom_map`, or has both, the mod spawns a vanilla wandering trader and sends/logs a warning.

### 3. Spawn one exact JSON profile

Use:

```text
pp_wtrader_specificprofile
```

Then add exactly one profile-type tag:

```text
custom_no_map
custom_map
```

Then set the armor stand CustomName to the exact loaded profile id.

Example:

```mcfunction
summon armor_stand ~ ~ ~ {Tags:["pp_wtrader_specificprofile","custom_no_map"],CustomName:'"pp_legendarydungeons:wtraders_nomap/random_cheap"',Invisible:1b,Marker:1b}
```

The profile's JSON `"trader_type"` must match the marker tag. For example:

```json
"trader_type": "custom_no_map"
```

must be used with marker tag:

```text
custom_no_map
```

If the marker is missing the type tag, missing the custom name, has an invalid profile id, references a missing profile, or references a profile with the wrong trader type, the mod spawns a vanilla wandering trader and sends/logs a warning.

## Trade Pool Entries with Loot Tables

Trade-pool entries can now use either:

```json
"item": "minecraft:diamond_pickaxe"
```

or:

```json
"loot_table": "pp_legendarydungeons:wtrader/items/fortune_3_pickaxe"
```

Use `item` for plain items. Use `loot_table` for enchanted items, random enchants, custom names/lore, custom components, or anything else that is easier to build with vanilla loot table functions.

Do not use both `item` and `loot_table` in the same entry.

### Example trade pool entry

```json
{
  "loot_table": "pp_legendarydungeons:wtrader/items/fortune_3_pickaxe",
  "weight": 10,
  "count_min": 1,
  "count_max": 1,
  "max_uses_min": 1,
  "max_uses_max": 1,
  "xp": 10,
  "tags": [
    "custom_enchants",
    "tool"
  ]
}
```

The trade entry's `count_min` / `count_max` still controls the final stack count in the merchant offer. For tools, keep both as `1`.

## Recommended Loot Table Folder

Use a dedicated folder for generated trade items:

```text
src/main/resources/data/pp_legendarydungeons/loot_table/wtrader/items/
```

Example loot table id:

```text
pp_legendarydungeons:wtrader/items/fortune_3_pickaxe
```

corresponds to:

```text
src/main/resources/data/pp_legendarydungeons/loot_table/wtrader/items/fortune_3_pickaxe.json
```
