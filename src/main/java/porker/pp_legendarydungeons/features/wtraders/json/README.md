# WTrader JSON Java Package

This package contains the Java-side model and loading support for the custom wandering trader JSON system.

The goal is to let JSON files define reusable weighted trade pools, trader profiles, map offers, and selection tables. Java then turns those definitions into Minecraft `MerchantOffer` objects and applies them to wandering traders, future trading caravans, custom villagers, or other NPC/shop systems.

## Package Location

```text
src/main/java/porker/pp_legendarydungeons/features/wtraders/json/
```

## Runtime JSON Folders

The future reload listener should read these folders:

```text
src/main/resources/data/pp_legendarydungeons/wtrader/trade_pools/
src/main/resources/data/pp_legendarydungeons/wtrader/profiles/
src/main/resources/data/pp_legendarydungeons/wtrader/selection_tables/
src/main/resources/data/pp_legendarydungeons/wtrader/map_offers/
```

These paths are represented by `WTraderJsonFolders`.

## Main Java Model Classes

### `TradePoolJson`

Represents one file from:

```text
data/<namespace>/wtrader/trade_pools/*.json
```

A trade pool defines a reusable weighted category of possible trades.

Examples:

```text
saplings
type_gems
apricorn_seeds
mint_seeds
joke_items
relic_coin_specials
ev_training_items
enchanted_tools
```

Important fields:

```text
id
category
categoryWeight
tags
basePrice
secondaryPrice
entries
```

Each entry in the pool is represented by `TradeEntryJson`.

### `TradeEntryJson`

Represents one possible sell item inside a trade pool.

Important fields:

```text
item
weight
countMin
countMax
maxUsesMin
maxUsesMax
xp
tags
```

The future trade generator will:

```text
1. Roll a valid trade pool.
2. Roll a weighted entry inside that pool.
3. Apply count variance.
4. Apply price variance.
5. Convert the result into a MerchantOffer.
```

### `TradePriceJson`

Represents a price item.

Example JSON:

```json
{
  "item": "minecraft:emerald",
  "count": 5,
  "variance_min": -1,
  "variance_max": 2
}
```

This can be used for either:

```text
base_price
secondary_price
```

If `secondary_price` is `null`, the trade only uses one cost item.

### `TraderProfileJson`

Represents one file from:

```text
data/<namespace>/wtrader_profiles/*.json
```

A trader profile defines one trader identity.

Examples:

```text
to_skypillar
to_crystal_caves
supply_trader
mining_trader
ev_training_trader
```

Important fields:

```text
id
displayName
showDisplayName
traderType
mapType
totalTrades
guaranteedTrades
randomSlots
```

`showDisplayName` controls whether the trader name is visible above the entity.

For testing:

```json
"show_display_name": true
```

For final gameplay, if the trader should not visibly show a custom nametag:

```json
"show_display_name": false
```

### `TraderTradeJson`

Represents one guaranteed trade inside a profile.

The first planned trade types are:

```text
direct_item
generated_map
pool_roll
```

These constants are stored in `WTraderJsonTradeTypes`.

#### `direct_item`

A fixed item trade defined inside the profile.

Example use:

```text
apple → cobblemon:leftovers
emeralds → cobblemon:flying_gem
```

#### `generated_map`

A map trade that points to a `MapOfferJson`.

The map offer then points to an existing map loot table.

The flow should be:

```text
Trader profile requests generated map
→ map offer gives loot table id
→ Java runs the loot table
→ generated map is inserted directly into the trade
```

The map loot table is responsible for the actual map destination and hidden custom data.

#### `pool_roll`

A guaranteed slot that still rolls from weighted trade pools.

Example use:

```text
Guaranteed trade 1 must be from type_gems.
Guaranteed trade 2 must be from joke_items.
Guaranteed trade 3 must be from relic_coin_specials.
```

### `TraderRandomSlotJson`

Represents a random slot in a trader profile.

Random slots use category and tag filters.

Example logic:

```text
Pick 1 trade from categories saplings or utility_items.
Exclude anything tagged legendary or master_ball.
```

Important fields:

```text
count
categoryFilterMode
categories
tagFilterMode
tags
```

Filter modes are stored in `WTraderJsonFilterModes`.

Supported planned modes:

```text
include
exclude
```

### `MapOfferJson`

Represents one file from:

```text
data/<namespace>/wtrader_map_offers/*.json
```

A map offer links a simple id to a map loot table.

Example:

```json
{
  "id": "pp_legendarydungeons:sky_pillar",
  "loot_table": "pp_legendarydungeons:maps/find_skypillar",
  "tags": ["single_target", "legendary_dungeon", "rayquaza"]
}
```

The trade system should not manually add map lore. The map loot table should generate the functional map and hidden custom data.

### `SelectionTableJson`

Represents one file from:

```text
data/<namespace>/wtrader_selection_tables/*.json
```

The default table should eventually control:

```text
20% vanilla wandering trader
40% custom no-map trader
40% custom map trader
```

Inside custom map traders, it should support:

```text
50% single-target map trader
25% random misc map trader
20% random research outpost map trader
5% random dungeon map trader
```

### Weighted Helper Classes

These classes represent weighted rolls inside selection tables:

```text
WeightedResultJson
WeightedProfileJson
WeightedProfileGroupJson
```

Examples:

```json
{ "result": "custom_map", "weight": 40 }
```

```json
{ "profile": "pp_legendarydungeons:to_skypillar", "weight": 10 }
```

## Helper Constants

### `WTraderJsonFolders`

Stores runtime folder names:

```text
wtrader_trade_pools
wtrader_profiles
wtrader_selection_tables
wtrader_map_offers
```

### `WTraderJsonTradeTypes`

Stores planned trade type ids:

```text
direct_item
generated_map
pool_roll
```

### `WTraderJsonFilterModes`

Stores planned filter mode ids:

```text
include
exclude
```

### `WTraderJsonTraderTypes`

Stores planned trader type ids:

```text
vanilla_wandering_trader
custom_no_map
custom_map
```

### `WTraderJsonValues`

Contains helper methods for nullable JSON fields.

The JSON model classes use nullable wrapper types like `Integer` and `Boolean` so the future loader can tell the difference between:

```text
field missing
field set to 0
field set to false
```

## Planned Implementation Order

### Step 1: JSON model classes

Current step.

These classes describe the shape of the JSON files but do not affect gameplay by themselves.

### Step 2: Registry classes

Next step.

Create a registry that stores loaded JSON data:

```text
Map<ResourceLocation, TradePoolJson>
Map<ResourceLocation, TraderProfileJson>
Map<ResourceLocation, SelectionTableJson>
Map<ResourceLocation, MapOfferJson>
```

### Step 3: Reload listener

Add a Fabric resource reload listener that scans:

```text
data/*/wtrader_trade_pools/*.json
data/*/wtrader_profiles/*.json
data/*/wtrader_selection_tables/*.json
data/*/wtrader_map_offers/*.json
```

The listener should:

```text
1. Find JSON files.
2. Parse them with Gson.
3. Validate required fields.
4. Log useful warnings for broken files.
5. Store valid definitions in the registry.
```

### Step 4: Trade generator

Add Java logic that converts loaded JSON definitions into `MerchantOffer` objects.

This should handle:

```text
weighted pool selection
weighted entry selection
category filters
tag filters
count variance
price variance
max use variance
direct item trades
generated map trades
pool-roll trades
```

### Step 5: Connect to existing wtrader spawning

Only after the loader and generator work should the existing `WanderingTraderCommandSpawner` switch from hardcoded Java profiles to JSON-loaded profiles.

This avoids breaking the currently working trader system too early.

## Important Design Boundary

The JSON model should stay entity-agnostic.

That means the JSON files define possible trades, not whether the trades belong to:

```text
wandering traders
caravans
custom villagers
vanilla villagers
future NPCs
```

Other Java systems can reuse the same generated `MerchantOffer`s later.
