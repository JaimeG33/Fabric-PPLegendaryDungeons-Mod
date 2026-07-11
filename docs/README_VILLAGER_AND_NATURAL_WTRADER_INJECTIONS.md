# Villager and Natural Wandering-Trader Integration

This patch adds two additive/data-driven trade systems:

1. Three new cartographer candidate trades backed by existing WTrader
   `trade_pool` JSON.
2. A 75% vanilla / 25% custom roll for naturally spawned wandering traders,
   backed by `vanilla_wtrader_spawns.json` and the existing WTrader profiles.

The patch targets Minecraft Fabric 1.21.1 and the current
`JaimeG33/Fabric-PPLegendaryDungeons-Mod` `master` branch.

---

## 1. Cartographer trade injection

Java registration:

```text
src/main/java/porker/pp_legendarydungeons/features/wtraders/villager/VillagerTradeInjectionRegistrar.java
```

Pools:

```text
src/main/resources/data/pp_legendarydungeons/wtrader/trade_pools/villagers/cartographer/random_misc_map.json
src/main/resources/data/pp_legendarydungeons/wtrader/trade_pools/villagers/cartographer/random_research_outpost_map.json
src/main/resources/data/pp_legendarydungeons/wtrader/trade_pools/villagers/cartographer/random_dungeon_map.json
```

### Resulting candidate lists

Cartographer level 1:

```text
Vanilla paper trade
Vanilla empty-map trade
Injected random misc map trade
Injected random research-outpost map trade
```

Vanilla normally selects two level-1 offers. Other mods may append more
candidates and naturally alter the odds.

Cartographer level 5:

```text
Vanilla globe banner-pattern trade
Injected random dungeon map trade
```

With no other level-5 cartographer injection, both are selected because vanilla
normally adds two offers for a profession level.

### Why this is compatible

The registrar uses Fabric's additive `TradeOfferHelper.registerVillagerOffers`.
It does not edit or replace `VillagerTrades.TRADES`.

Existing villagers keep offers already saved to them. Use newly created,
newly employed, or newly leveled cartographers when testing changes.

---

## 2. How the three pools work

Each pool uses one of the existing functional map loot tables:

```text
pp_legendarydungeons:maps/random/find_random_misc
pp_legendarydungeons:maps/random/find_random_research_outpost
pp_legendarydungeons:maps/random/find_random_dungeon
```

The Java listing requests one exact pool by ID. The pool controls:

- Base price.
- Optional second item cost.
- Price variance.
- Maximum uses.
- Villager XP.
- Price multiplier.
- The loot table that creates the actual functional map.

Every pool includes:

```json
"tags": [
  "villager_only"
]
```

`WTraderJsonTradeGenerator` now treats that tag as a usage restriction:

- Broad profile `pool_roll` searches skip `villager_only` pools.
- `VillagerTradeInjectionRegistrar` can still request them directly by ID.

This prevents the cartographer pools from appearing in unrelated custom
wandering-trader random slots.

### Initial prices supplied by the patch

Random misc map:

```text
10-14 emeralds + 1 compass
1 use
1 villager XP
price_multiplier 0.05
```

Random research-outpost map:

```text
14-18 emeralds + 1 compass
1 use
1 villager XP
price_multiplier 0.05
```

Random dungeon map:

```text
20-28 emeralds + 1 compass
1 use
30 villager XP
price_multiplier 0.20
```

These are starting balance values. Edit the three pool JSON files to change
them without changing Java.

---

## 3. `price_multiplier` addition

Modified model:

```text
src/main/java/porker/pp_legendarydungeons/features/wtraders/json/TradePoolJson.java
```

A trade pool may now include:

```json
"price_multiplier": 0.05
```

When omitted, the old default of `0.05` is retained.

The validator accepts finite values from `0.0` through `1.0`. Typical vanilla
values are:

```text
0.05 = ordinary/low-tier trade
0.20 = expensive/high-tier trade
```

The generator now applies the pool's multiplier to offers generated from that
pool.

---

## 4. Direct loot-table stack generation

New runtime utility:

```text
src/main/java/porker/pp_legendarydungeons/features/wtraders/runtime/LootTableTradeStackFactory.java
```

It executes a Minecraft loot table directly with a chest-style loot context:

```text
ORIGIN = merchant position
THIS_ENTITY = merchant
```

It returns the first non-empty generated stack.

This avoids:

- Running `/loot` commands.
- Spawning a temporary dropped item.
- Searching for and capturing that item entity.
- Requiring a structure marker.

The functional exploration map is generated only when its candidate is actually
selected.

---

## 5. Natural wandering-trader behavior

Selection table:

```text
src/main/resources/data/pp_legendarydungeons/wtrader/selection_tables/vanilla_wtrader_spawns.json
```

Default top-level weights:

```text
75 vanilla_wandering_trader
15 custom_no_map
10 custom_map
```

Therefore, a naturally spawned trader has:

```text
75% unaltered vanilla offers
15% existing custom no-map profile
10% existing custom map profile
```

The profile lists and group weights initially mirror the current `default`
selection table. They can be edited independently.

### What remains vanilla

The patch changes only the offer list and optional profile display name after a
custom result succeeds. It preserves the same natural trader entity, including:

- Vanilla spawn timing and spawn chance.
- Vanilla llamas.
- Vanilla despawn timer.
- Vanilla wander target and home restriction.
- Vanilla tracking as the world's current wandering trader.

If profile/map generation fails, the normal offers are kept.

### Natural-only detection

Mixin:

```text
src/main/java/porker/pp_legendarydungeons/mixin/trades/NaturalWanderingTraderMixin.java
```

The mixin runs after `WanderingTrader.updateTrades()`.

The service then compares the trader's UUID with:

```text
ServerLevelData.getWanderingTraderId()
```

Only the trader currently tracked by vanilla's natural spawner is modified in
production. Structure traders, spawn eggs, `/summon`, and other mods' traders
are ignored.

A persistent entity tag prevents rerolling:

```text
pp_natural_wtrader_roll_complete
```

A successful custom result also receives:

```text
pp_natural_custom_trader
```

### Development test switch

In:

```text
src/main/java/porker/pp_legendarydungeons/features/wtraders/natural/NaturalWanderingTraderService.java
```

the production setting is:

```java
private static final boolean ALLOW_NON_NATURAL_TRADERS_FOR_TESTING = false;
```

Temporarily setting it to `true` lets `/summon minecraft:wandering_trader`
exercise the selection logic. Restore it to `false` before release.

---

## 6. Selection-table API changes

Modified:

```text
src/main/java/porker/pp_legendarydungeons/features/wtraders/json/WTraderJsonProfileSelector.java
```

It now supports:

```java
pickTopLevelResult(random, selectionTableId)
pickNoMapProfile(random, selectionTableId)
pickMapProfile(random, selectionTableId)
```

The overloads with a table ID are strict and do not fall back to an unrelated
profile. This is important for natural traders: a missing or broken
`vanilla_wtrader_spawns` table leaves the trader vanilla.

The original no-argument profile methods remain and preserve the existing
structure-trader fallback behavior.

The existing structure-marker spawner's hardcoded 20/40/40 top-level roll is
not changed by this patch.

---

## 7. Adding another villager pool later

Create a JSON file under:

```text
src/main/resources/data/pp_legendarydungeons/wtrader/trade_pools/villagers/
```

Give it a unique ID and include `villager_only`.

Then add a pool constant and listing in:

```text
src/main/java/porker/pp_legendarydungeons/features/wtraders/villager/VillagerTradeInjectionRegistrar.java
```

General syntax:

```java
private static final ResourceLocation MY_POOL =
        modId("villagers/profession/my_pool");

TradeOfferHelper.registerVillagerOffers(
        VillagerProfession.CLERIC,
        2,
        factories -> factories.add(new TradePoolListing(MY_POOL))
);
```

Level numbers:

```text
1 novice
2 apprentice
3 journeyman
4 expert
5 master
```

---

## 8. Testing cartographers

Build and launch, then summon fresh cartographers.

Novice:

```mcfunction
/summon minecraft:villager ~ ~ ~ {VillagerData:{type:"minecraft:plains",profession:"minecraft:cartographer",level:1}}
```

Master:

```mcfunction
/summon minecraft:villager ~ ~ ~ {VillagerData:{type:"minecraft:plains",profession:"minecraft:cartographer",level:5}}
```

Interact with each new villager.

Expected novice behavior:

- Two offers selected from paper, empty map, misc map, and research map.
- Summon several villagers because the two injected candidates are not
  guaranteed.

Expected master behavior without another cartographer trade mod:

- Globe banner pattern.
- Random dungeon map.

The map loot table may fail when it cannot locate an eligible target structure.
In that case the selected candidate can return no offer, just like vanilla
explorer-map factories can fail.

---

## 9. Testing natural custom wandering traders quickly

1. Temporarily set:

```java
ALLOW_NON_NATURAL_TRADERS_FOR_TESTING = true;
```

2. Temporarily simplify `top_level_rolls` in:

```text
src/main/resources/data/pp_legendarydungeons/wtrader/selection_tables/vanilla_wtrader_spawns.json
```

For a guaranteed no-map test:

```json
"top_level_rolls": [
  {
    "result": "custom_no_map",
    "weight": 100
  }
]
```

For a guaranteed map test:

```json
"top_level_rolls": [
  {
    "result": "custom_map",
    "weight": 100
  }
]
```

3. Rebuild and launch.

4. Summon and interact:

```mcfunction
/summon minecraft:wandering_trader ~ ~ ~
```

5. Check `logs/latest.log` for:

```text
[Natural WTrader] Replaced natural trader ...
```

6. Restore:

```java
ALLOW_NON_NATURAL_TRADERS_FOR_TESTING = false;
```

7. Restore the 75/15/10 JSON weights before release.

When the test flag is false, `/summon` is intentionally ignored.

---

## 10. Multiplayer behavior

The natural selection roll is stored on each trader with a persistent entity
tag, so separate traders do not share state.

All offer generation runs on the server. The natural service changes the
specific trader's saved `MerchantOffers`; every player sees the same offers.

Cartographer factories use vanilla's normal per-villager offer generation and
save the resulting map and prices with that villager.

---

## 11. Build command

From the repository root:

```powershell
.\gradlew.bat clean build
```

The output JAR appears under:

```text
build/libs/
```
