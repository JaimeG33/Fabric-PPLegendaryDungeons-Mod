# Wandering Trader JSON Schema

This document explains the planned custom trade JSON system for `pp_legendarydungeons`.

The goal is to define reusable weighted trade pools in JSON, then let Java decide where those trades are used. The same trade pools should eventually be usable by structure-spawned wandering traders, trading caravans, custom villagers, village-spawned traders, and future NPC/shop systems.

## Recommended Folder Layout

Runtime JSON files, loaded by the mod after the Java loader is implemented:

```text
src/main/resources/data/pp_legendarydungeons/wtrader/trade_pools/
src/main/resources/data/pp_legendarydungeons/wtrader/profiles/
src/main/resources/data/pp_legendarydungeons/wtrader/selection_tables/
src/main/resources/data/pp_legendarydungeons/wtrader/map_offers/
```

Documentation and safe examples that do not load at runtime:

```text
docs/wtrader_json_schema.md
docs/examples/wtraders/
```

## Main File Types

1. **Trade pools** define reusable weighted trade categories such as saplings, type gems, apricorn seeds, joke items, EV training items, and relic coin specials.
2. **Trader profiles** define a specific trader identity, such as `to_skypillar`, `supply_trader`, or `mining_trader`.
3. **Trader selection tables** define spawn odds between vanilla traders, custom no-map traders, and custom map traders.
4. **Map offers** connect a map offer id to an existing map loot table. The loot table still generates the actual functional map.

## Design Rules

Use `weight`, not percentage chance. The code will add all valid weights and roll from the total.

Use explicit min/max fields for the first version:

```json
"count_min": 1,
"count_max": 2,
"max_uses_min": 2,
"max_uses_max": 4
```

Avoid shorthand like `"count": [1, 2]` in the first version. The explicit version is uglier, but easier to parse and validate.

## Trader Display Names

Every trader profile should include:

```json
"display_name": "Sky Pillar Map Trader",
"show_display_name": true
```

`show_display_name` controls whether the name tag is visible above the entity. Keep it `true` while testing. Later, set it to `false` for traders that should not visibly show custom names in-game.

## Filter Modes

Random slots can filter by categories and tags.

Include mode allows only listed values:

```json
"category_filter_mode": "include",
"categories": ["saplings", "type_gems"]
```

Exclude mode allows everything except listed values:

```json
"category_filter_mode": "exclude",
"categories": ["maps"]
```

The same pattern applies to tags:

```json
"tag_filter_mode": "include",
"tags": ["relic_coin"]
```

or:

```json
"tag_filter_mode": "exclude",
"tags": ["legendary", "master_ball"]
```

## Trade Pool Template

```json
{
  "id": "pp_legendarydungeons:saplings",
  "category": "saplings",
  "category_weight": 20,
  "tags": ["nature", "cheap", "emerald_only"],
  "base_price": {
    "item": "minecraft:emerald",
    "count": 5,
    "variance_min": -1,
    "variance_max": 2
  },
  "secondary_price": null,
  "entries": [
    {
      "item": "minecraft:oak_sapling",
      "weight": 10,
      "count_min": 1,
      "count_max": 1,
      "max_uses_min": 2,
      "max_uses_max": 4,
      "xp": 3,
      "tags": ["overworld", "sapling"]
    }
  ]
}
```

## Trader Profile Template

```json
{
  "id": "pp_legendarydungeons:to_skypillar",
  "display_name": "Sky Pillar Map Trader",
  "show_display_name": true,
  "trader_type": "custom_map",
  "map_type": "single_target",
  "total_trades": 5,
  "guaranteed_trades": [
    {
      "type": "generated_map",
      "map_offer": "pp_legendarydungeons:sky_pillar",
      "base_price": {
        "item": "minecraft:emerald",
        "count": 32,
        "variance_min": -4,
        "variance_max": 4
      },
      "secondary_price": {
        "item": "cobblemon:relic_coin",
        "count": 32,
        "variance_min": -4,
        "variance_max": 4
      },
      "max_uses_min": 1,
      "max_uses_max": 1,
      "xp": 10
    }
  ],
  "random_slots": [
    {
      "count": 1,
      "category_filter_mode": "include",
      "categories": ["saplings", "relic_coin_tools"],
      "tag_filter_mode": "exclude",
      "tags": ["master_ball", "legendary"]
    }
  ]
}
```

## Selection Table Template

```json
{
  "id": "pp_legendarydungeons:default",
  "top_level_rolls": [
    { "result": "vanilla_wandering_trader", "weight": 20 },
    { "result": "custom_no_map", "weight": 40 },
    { "result": "custom_map", "weight": 40 }
  ],
  "custom_no_map_profiles": [
    { "profile": "pp_legendarydungeons:supply_trader", "weight": 10 }
  ],
  "custom_map_profile_groups": [
    {
      "group": "single_target",
      "weight": 50,
      "profiles": [
        { "profile": "pp_legendarydungeons:to_skypillar", "weight": 10 },
        { "profile": "pp_legendarydungeons:to_crystal_caves", "weight": 10 }
      ]
    },
    {
      "group": "random_misc",
      "weight": 25,
      "profiles": [
        { "profile": "pp_legendarydungeons:random_misc_map_trader", "weight": 10 }
      ]
    },
    {
      "group": "random_research_outpost",
      "weight": 20,
      "profiles": [
        { "profile": "pp_legendarydungeons:random_research_outpost_map_trader", "weight": 10 }
      ]
    },
    {
      "group": "random_dungeon",
      "weight": 5,
      "profiles": [
        { "profile": "pp_legendarydungeons:random_dungeon_map_trader", "weight": 10 }
      ]
    }
  ]
}
```

## Map Offer Template

```json
{
  "id": "pp_legendarydungeons:sky_pillar",
  "loot_table": "pp_legendarydungeons:maps/find_skypillar",
  "tags": ["single_target", "legendary_dungeon", "rayquaza"]
}
```

The trade system should not manually add map lore. The map loot table should generate the actual functional map and set hidden custom data.

Recommended flow:

```text
Trader profile requests a generated map
→ Java runs the configured map loot table
→ loot table creates the functional map
→ Java inserts that generated map directly into the trade
→ item gimmick system adds visible lore/coordinates only after the player owns the map
```

## Trade Types

The first version should support:

- `direct_item`: fixed item trade defined directly in the trader profile.
- `generated_map`: map trade using a map offer id.
- `pool_roll`: weighted random trade pulled from matching trade pools.

## Notes for Future Villager/Caravan Support

The trade pool JSON should stay entity-agnostic. It should define possible trades, not how those trades are applied.

Java can later apply generated `MerchantOffer`s to structure-spawned wandering traders, trading caravan wandering traders, custom villagers, vanilla profession trade injections, or future NPC/shop systems.
