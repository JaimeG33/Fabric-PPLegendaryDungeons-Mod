# Server Tick Scheduler

`ServerTickScheduler` is the mod's only Architectury server-tick event
registration.

It delegates to focused subsystems rather than placing gameplay logic in one
large class:

```text
EntityLegendarySummonTicker
FeatureTicker
DungeonPokemonManager
ItemGimmickTicker
SecretTicker
```

Each subsystem owns its interval, indexes, state, and cleanup.

## Dungeon Pokémon scaling

`DungeonPokemonManager` tracks only entities created or restored by the dungeon
Pokémon system.

It does not:

- Scan all entities in every dimension.
- Scan all Pokémon near every player.
- Force chunks to remain loaded.
- Reapply effects every game tick.
- Register one tick listener per Pokémon.

Each record is assigned a deterministic update bucket from its UUID. This
spreads work across ticks while preserving each profile's configured update
interval.

Target searches are:

- Bounded by `detection_range`.
- Performed only for loaded managed Pokémon.
- Reused while the current target remains valid.
- Split between the level player list and one bounded living-entity query for
  non-player target rules.

Entity persistence is event-driven through Cobblemon save/load events. Captures
are cleaned up through `POKEMON_CAPTURED`.

## Adding another scheduled system

1. Keep the gameplay implementation in its own package.
2. Expose a method such as `tick(MinecraftServer, long)`.
3. Add one explicit call in `ServerTickScheduler`.
4. Keep expensive work behind an interval.
5. Prefer UUID/index lookups over repeated world scans.
6. Clear static runtime state during `SERVER_STOPPED`.
