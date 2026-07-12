# Villagers and Wandering Traders

## Cartographer injections

The mod uses Fabric’s additive villager trade helper. It does not replace the vanilla trade map.

### Novice cartographer candidates

```text
Vanilla paper trade
Vanilla empty-map trade
Injected random misc map
Injected random research-outpost map
```

Vanilla selects two candidates. Other trade-injection mods can change the odds by adding more candidates.

### Master cartographer candidates

```text
Vanilla globe banner-pattern trade
Injected random dungeon map
```

With no other master cartographer additions, both are selected.

## Villager-only pools

```text
pp_legendarydungeons:villagers/cartographer/random_misc_map
pp_legendarydungeons:villagers/cartographer/random_research_outpost_map
pp_legendarydungeons:villagers/cartographer/random_dungeon_map
```

Each pool contains:

```json
"tags": [
  "villager_only"
]
```

Broad WTrader pool rolls skip these pools. The villager registrar requests them directly.

Current balance:

| Trade | Base emeralds | Variance | Secondary cost | Uses | XP | Multiplier |
|---|---:|---:|---|---:|---:|---:|
| Random misc map | 12 | -6 to +2 | 16 relic coins, -4 to 0 | 1 | 1 | 0.05 |
| Research-outpost map | 8 | -5 to +3 | 8 relic coins, -4 to 0 | 1 | 1 | 0.05 |
| Random dungeon map | 28 | -4 to +4 | 28 relic coins, -4 to +4 | 1 | 30 | 0.20 |

## Structure-spawned WTraders

Structure markers can request:

```text
rolled trader
profile-type trader
specific JSON profile
regular unmodified wandering trader
```

JSON profiles generate their complete offer list before the custom trader is finalized.

## Natural wandering traders

A mixin runs after vanilla generates a wandering trader’s normal offers. The service verifies that the trader UUID matches the one stored by vanilla’s natural spawner.

Default selection table:

```text
pp_legendarydungeons:vanilla_wtrader_spawns
```

Default odds:

```text
75 vanilla_wandering_trader
15 custom_no_map
10 custom_map
```

A custom profile is fully generated before the vanilla offers are cleared. Failure leaves the trader unmodified.

Preserved vanilla behavior:

```text
same entity
same llamas
same despawn timer
same wander target
same world tracking
```

Persistent tag preventing rerolls:

```text
pp_natural_wtrader_roll_complete
```

Custom natural-trader tag:

```text
pp_natural_custom_trader
```

## Testing switch

Production value:

```java
private static final boolean ALLOW_NON_NATURAL_TRADERS_FOR_TESTING = false;
```

For a local test only, set it to true, force one custom result to weight 100 in `vanilla_wtrader_spawns.json`, summon a wandering trader, and interact with it.

Restore the constant and selection weights before release.
