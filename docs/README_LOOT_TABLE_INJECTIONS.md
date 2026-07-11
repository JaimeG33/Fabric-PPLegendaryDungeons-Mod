# Additive Loot Table Injections

This document describes the vanilla chest and entity loot-table injections used
by `pp_legendarydungeons` on Minecraft 1.21.1 + Fabric.

## Purpose

The injection system adds independent loot pools to selected existing loot
tables. It does **not** replace the original vanilla table.

This means the target table keeps:

- its original vanilla pools;
- additive pools registered by other Fabric mods;
- this mod's nested injection pool.

The Java integration uses Fabric API `LootTableEvents.MODIFY`. It intentionally
does not use `LootTableEvents.REPLACE` and does not place replacement files in
the `minecraft` namespace.

## Java Registration

Main registrar:

```text
src/main/java/porker/pp_legendarydungeons/loot/LootTableInjectionRegistrar.java
```

Main initializer registration:

```text
src/main/java/porker/pp_legendarydungeons/ProfessorPorkersLegendaryDungeons.java
```

The registrar groups one reusable injection loot table with a Java `Set` of
existing target loot-table IDs. This behaves like a small, explicit target tag
without requiring a separate datapack tag/reload lookup system.

## Injection Loot Tables

All injection-specific JSON is kept under:

```text
src/main/resources/data/pp_legendarydungeons/loot_table/loot_injections/
```

Current tables:

```text
src/main/resources/data/pp_legendarydungeons/loot_table/loot_injections/chests/village_random_maps.json
src/main/resources/data/pp_legendarydungeons/loot_table/loot_injections/entities/dungeon_key_base.json
```

These tables reference the mod's canonical reward tables rather than copying
their item definitions.

### Village Random Maps

Injection table ID:

```text
pp_legendarydungeons:loot_injections/chests/village_random_maps
```

Current total chance per eligible chest:

```text
10%
```

When the 10% roll succeeds, the selected map is weighted:

- Random Misc map: weight 3, or 60% of successful map rolls.
- Random Research Outpost map: weight 2, or 40% of successful map rolls.

This gives an effective per-chest chance of approximately:

- 6% Random Misc map.
- 4% Random Research Outpost map.

Canonical map tables referenced:

```text
pp_legendarydungeons:maps/random/find_random_misc
pp_legendarydungeons:maps/random/find_random_research_outpost
```

Eligible vanilla targets are listed in `VILLAGE_RANDOM_MAP_TARGETS`:

```text
minecraft:chests/village/village_cartographer
minecraft:chests/village/village_desert_house
minecraft:chests/village/village_plains_house
minecraft:chests/village/village_savanna_house
minecraft:chests/village/village_snowy_house
minecraft:chests/village/village_taiga_house
```

Vanilla has a dedicated cartographer chest loot table, but no dedicated
librarian chest loot table. The generic biome house tables broaden map access to
ordinary village residential chests.

### Dungeon Key Boss Drops

Injection table ID:

```text
pp_legendarydungeons:loot_injections/entities/dungeon_key_base
```

Current chance:

```text
10%, only when killed by a player
```

Canonical key table referenced:

```text
pp_legendarydungeons:key_dungeon_base
```

This is important because the canonical table creates the functional Ancient
Key with the current `minecraft:custom_data` identity used by the configured
vault.

Eligible vanilla entity targets are listed in `DUNGEON_KEY_BASE_TARGETS`:

```text
minecraft:entities/warden
minecraft:entities/elder_guardian
```

This does not edit or replace the mod's separate custom dungeon elder-guardian
loot table:

```text
src/main/resources/data/pp_legendarydungeons/loot_table/entities/dungeon_eguardian.json
```

## Respecting Datapack Replacements

The registrar checks:

```java
source.isBuiltin()
```

This applies the injection to vanilla tables and tables bundled by mods. It
skips tables supplied by an external datapack and tables created by Fabric's
replacement event. The intent is to respect a pack author's deliberate full
override while remaining compatible with other additive modifiers.

Remove this check only if the mod should force its injection into external
replacement tables as well.

## Changing Chances

Village map chance:

```text
src/main/resources/data/pp_legendarydungeons/loot_table/loot_injections/chests/village_random_maps.json
```

Change:

```json
"chance": 0.1
```

Examples:

- `0.05` = 5%
- `0.1` = 10%
- `0.25` = 25%
- `1.0` = 100% for testing

Change the map entry weights to rebalance which map is selected after the chance
succeeds.

Boss key chance:

```text
src/main/resources/data/pp_legendarydungeons/loot_table/loot_injections/entities/dungeon_key_base.json
```

Change its `minecraft:random_chance` value. Remove the
`minecraft:killed_by_player` condition only if environmental deaths should also
be able to drop the key.

## Adding More Target Loot Tables

Open:

```text
src/main/java/porker/pp_legendarydungeons/loot/LootTableInjectionRegistrar.java
```

To add another chest target, add another line inside:

```java
VILLAGE_RANDOM_MAP_TARGETS
```

Example:

```java
vanillaLootTable("chests/village/village_temple")
```

To add another vanilla boss or miniboss target, add another line inside:

```java
DUNGEON_KEY_BASE_TARGETS
```

Example:

```java
vanillaLootTable("entities/wither_skeleton")
```

Use the exact loot-table path, not merely the entity registry ID. Verify new
paths before adding them.

To target a loot table owned by another mod, add a helper call or create the key
with that mod's namespace. Do not use `vanillaLootTable(...)` for non-Minecraft
namespaces.

## Adding a New Injection Category

1. Add a new JSON table somewhere under:

   ```text
   src/main/resources/data/pp_legendarydungeons/loot_table/loot_injections/
   ```

2. Add a `ResourceKey<LootTable>` constant for it in:

   ```text
   src/main/java/porker/pp_legendarydungeons/loot/LootTableInjectionRegistrar.java
   ```

3. Add a `Set<ResourceKey<LootTable>>` containing the target tables.

4. Add a new `InjectionGroup` to `INJECTION_GROUPS`.

The Fabric callback will then attach the same nested injection table to every
listed target.

## Testing

### Confirm the canonical key

```mcfunction
/loot give @s loot pp_legendarydungeons:key_dungeon_base
```

The resulting key should have the correct name, lore, rarity, and custom data.

### Test the village injection table by itself

Temporarily set the chance to `1.0`, then run:

```mcfunction
/loot give @s loot pp_legendarydungeons:loot_injections/chests/village_random_maps
```

This validates the injection JSON and map generation. It does not prove that the
vanilla target hook fired, so also test a newly generated eligible village
chest.

### Test the entity injection table

Because the table requires `minecraft:killed_by_player`, the most representative
test is to kill a newly summoned target while the injection chance is
temporarily `1.0`.

Example:

```mcfunction
/summon minecraft:elder_guardian ~ ~ ~
```

Kill it in survival and confirm that its normal drops remain and the Ancient Key
is added.

### Reload and logs

After changing JSON:

```mcfunction
/reload
```

After changing Java, rebuild and replace the old mod JAR:

```powershell
.\gradlew.bat clean build
```

Expected startup log:

```text
[Loot Injection] Registered 2 additive loot-table injection groups.
```

## Existing Worlds and Containers

- Future entity deaths use the current loaded loot tables.
- A chest that has already generated its contents will not be retroactively
  changed.
- A newly generated or still-unresolved eligible chest can use the modified
  table when its loot is generated.

## Map-Search Performance Warning

The two map tables use `minecraft:exploration_map`. Resolving a map can require a
server-side structure search when the chest loot is generated. Keep the chance
modest and test in a representative world. If random map generation causes a
noticeable freeze, future work should cache or precompute structure targets.

## Not Included Yet

This system modifies ordinary Minecraft loot tables. Cobblemon battle reward
injection is not included here because Pokémon battle rewards may use Cobblemon
battle/drop APIs rather than the vanilla entity death loot-table path.
