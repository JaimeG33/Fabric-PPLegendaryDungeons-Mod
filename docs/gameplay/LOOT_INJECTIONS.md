# Loot Injections

## Vanilla Minecraft loot tables

`LootTableInjectionRegistrar` uses Fabric’s additive `LootTableEvents.MODIFY` event. It appends an independent nested pool and does not replace vanilla or other mods’ pools.

Current injection groups:

### Village random maps

Injection table:

```text
pp_legendarydungeons:loot_injections/chests/village_random_maps
```

Target:

```text
minecraft:chests/village/village_cartographer
```

Base weights at Luck 0:

```text
Nothing: 87
Random misc map: 8
Random research-outpost map: 5
```

Both map entries have quality 5, so positive Luck raises their effective weights relative to the empty result.

### Snow village extras

Injection table:

```text
pp_legendarydungeons:loot_injections/chests/snow_village_extras
```

Targets:

```text
minecraft:chests/village/village_taiga_house
minecraft:chests/igloo_chest
```

Base weights at Luck 0:

```text
Nothing: 96
Research-outpost map: 1, quality 1
Misc map: 1, quality 2
Random dungeon map: 1, quality 3
Ancient Key: 1, quality 4
```

### Boss and miniboss key drop

Injection table:

```text
pp_legendarydungeons:loot_injections/entities/dungeon_key_base
```

Targets:

```text
minecraft:entities/warden
minecraft:entities/elder_guardian
minecraft:entities/wither
```

The table requires a player-attributed kill.

Base weights at Luck 0:

```text
Nothing: 6
Ancient Key: 3, quality 7
```

The pool-level `random_chance` is currently 1.0, so the weighted empty/key selection is the actual probability gate.

## Cobblemon Pokémon drop bridge

Cobblemon species drops are not ordinary Minecraft entity loot tables. The mod listens to:

```text
CobblemonEvents.BATTLE_FAINTED
CobblemonEvents.LOOT_DROPPED
```

`BATTLE_FAINTED` records temporary battle/player context. `LOOT_DROPPED` runs the additional Minecraft loot table once and leaves Cobblemon’s native drops untouched.

Current mapping:

```text
cobblemon:klefki
→ pp_legendarydungeons:loot_injections/pokemon/klefki
```

Klefki base weights at Luck 0:

```text
Nothing: 90
Trial Key: 8, quality 5
Ancient Key: 2, quality 10
```

The table requires a player-attributed kill. Normal battle victories and direct player kills qualify; `/kill` without player attribution does not.

## Adding another vanilla target

Add the target key to the appropriate Java `Set` in:

```text
src/main/java/porker/pp_legendarydungeons/loot/LootTableInjectionRegistrar.java
```

## Adding another Pokémon

1. Create:

   ```text
   data/pp_legendarydungeons/loot_table/loot_injections/pokemon/species_name.json
   ```

2. Add the species-to-table entry in:

   ```text
   PokemonLootTableRunner.SPECIES_LOOT_TABLES
   ```

3. Test battle defeat and direct player kill separately.

## Testing

```mcfunction
/loot give @s loot pp_legendarydungeons:loot_injections/chests/village_random_maps
/loot give @s loot pp_legendarydungeons:loot_injections/chests/snow_village_extras
/loot give @s loot pp_legendarydungeons:loot_injections/entities/dungeon_key_base
/loot give @s loot pp_legendarydungeons:loot_injections/pokemon/klefki
```

Direct `/loot give` does not reproduce every entity context condition. Use real player kills for final entity-table tests.
