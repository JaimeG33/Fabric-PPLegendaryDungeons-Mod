# Legendary Summons and Dungeon Rules

## Central legendary encounter flow

```text
Trigger context
→ LegendarySummonService
→ registered legendary definition
→ legendary condition prepares the context
→ PokemonSpawnService resolves a reusable JSON spawn profile
→ legendary aftermath applies locks, tags, sounds, effects, and completion tracking
```

Legendary triggers, conditions, aftermath, and one-time locks remain
legendary-specific. Only the low-level Cobblemon entity creation was generalized.

Built-in Rayquaza definitions reference:

```text
pp_legendarydungeons:legendary/rayquaza/normal
pp_legendarydungeons:legendary/rayquaza/secret
```

Each definition also retains a code fallback property string so malformed
external data cannot permanently disable the existing encounter.

## General Pokémon spawning

Reusable generation data is documented in:

```text
docs/development/POKEMON_SPAWN_PROFILE_SCHEMA.md
```

The same `PokemonSpawnService` may be called by:

- Legendary encounter logic.
- Deterministic dungeon Pokémon markers.
- Future non-hostile or miscellaneous structure features.

It supports ordinary Cobblemon property strings plus structured level, nature,
shiny, IV, persistence, and spawn-cap settings.

## Rayquaza completion

After a normal Rayquaza is successfully created, the mod stores a persistent
completion watch using the spawned entity UUID.

Sky Pillar completes when the tracked Rayquaza:

```text
is no longer alive
becomes owned/captured
moves beyond the configured completion radius
is confirmed missing while its last known chunk is loaded
```

Unloaded chunks are not treated as a defeat.

## Dungeon Pokémon encounters

Fixed Pokémon guards and minibosses use the existing `pp_feature` structure
activation system:

```text
pp_feature
→ DungeonPokemonFeature
→ dungeon Pokémon profile
→ reusable spawn profile
→ PokemonSpawnService
→ DungeonPokemonManager
```

See:

```text
docs/gameplay/DUNGEON_POKEMON_ENCOUNTERS.md
```

## Dungeon-rule system

Dungeon rules are attached to active dungeon instances rather than global
gamerules. The system can control:

```text
block breaking
block placement
explosions
interaction restrictions
instance completion and cleanup
```

Dungeon Pokémon profiles may optionally require their linked instance to remain
active before applying aggression.

Block and zone data is registered before worlds load. Server lifecycle callbacks
initialize scoreboards and dungeon saved data.

## Multiplayer principles

```text
per-instance records
persistent entity NBT
UUID-based encounter tracking
server-authoritative changes
no global “last player” or “last dungeon” state
bounded managed-entity updates
```

## Crystal Caves

The Crystal Caves gameplay assets may remain in the source tree, but the dungeon
is not registered or advertised for 1.0.0. Do not create active summon markers
or map routes to it in release content until that dungeon is enabled.
