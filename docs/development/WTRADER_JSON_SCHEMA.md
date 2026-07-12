# WTrader JSON Schema

## Runtime folders

```text
data/pp_legendarydungeons/wtrader/trade_pools/
data/pp_legendarydungeons/wtrader/profiles/
data/pp_legendarydungeons/wtrader/selection_tables/
data/pp_legendarydungeons/wtrader/map_offers/
```

The loader scans these folders recursively.

## Trade pool

```json
{
  "id": "pp_legendarydungeons:example_pool",
  "category": "example",
  "category_weight": 10,
  "tags": [
    "example"
  ],
  "base_price": {
    "item": "minecraft:emerald",
    "count": 8,
    "variance_min": -2,
    "variance_max": 2
  },
  "secondary_price": null,
  "price_multiplier": 0.05,
  "entries": [
    {
      "item": "minecraft:apple",
      "weight": 10,
      "count_min": 1,
      "count_max": 2,
      "max_uses_min": 2,
      "max_uses_max": 4,
      "xp": 3,
      "tags": [
        "food"
      ]
    }
  ]
}
```

An entry must provide either:

```json
"item": "namespace:item"
```

or:

```json
"loot_table": "namespace:path"
```

Use a loot table for generated maps, enchanted items, or component-heavy custom rewards.

Optional pool field:

```json
"price_multiplier": 0.05
```

Default is 0.05.

Restricted pool tag:

```json
"villager_only"
```

This prevents broad WTrader `pool_roll` searches from selecting the pool. A direct pool request can still use it.

## Trader profile

```json
{
  "id": "pp_legendarydungeons:example_profile",
  "display_name": "Example Trader",
  "show_display_name": false,
  "trader_type": "custom_no_map",
  "map_type": "none",
  "total_trades": 5,
  "guaranteed_trades": [],
  "random_slots": [
    {
      "count": 5,
      "category_filter_mode": "exclude",
      "categories": [],
      "tag_filter_mode": "exclude",
      "tags": [
        "villager_only"
      ]
    }
  ]
}
```

Supported trader types:

```text
custom_no_map
custom_map
```

Primary trade types:

```text
direct_item
generated_map
pool_roll
```

## Map offer

```json
{
  "id": "pp_legendarydungeons:sky_pillar",
  "loot_table": "pp_legendarydungeons:maps/find_skypillar",
  "tags": [
    "single_target",
    "legendary_dungeon",
    "rayquaza"
  ]
}
```

The map loot table creates the functional filled map. The profile does not manually recreate map components.

## Selection table

```json
{
  "id": "pp_legendarydungeons:vanilla_wtrader_spawns",
  "top_level_rolls": [
    {
      "result": "vanilla_wandering_trader",
      "weight": 75
    },
    {
      "result": "custom_no_map",
      "weight": 15
    },
    {
      "result": "custom_map",
      "weight": 10
    }
  ],
  "custom_no_map_profiles": [],
  "custom_map_profile_groups": []
}
```

Top-level result IDs:

```text
vanilla_wandering_trader
custom_no_map
custom_map
```

All weights are relative weights, not literal percentages unless their total is 100.

## Filter modes

```text
include
exclude
include_any_of
```

Use categories for broad content families and tags for capability/restriction metadata.

## Reload behavior

JSON resources reload through the server resource manager. New profiles and pool balance changes can take effect after `/reload`, but villagers and merchants that already saved offers are not retroactively rebuilt.
