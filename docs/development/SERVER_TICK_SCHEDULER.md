# Server Tick Scheduler

`ServerTickScheduler` is the mod's only Architectury server-tick event
registration.

It delegates to focused subsystems rather than placing gameplay logic in one
large class:

```text
EntityLegendarySummonTicker
FeatureTicker
DungeonPokemonManager
DungeonMobManager
ItemGimmickTicker
SecretTicker
```

Each subsystem owns its interval, indexes, state, and cleanup.

## Dungeon encounter scaling

`DungeonPokemonManager` tracks only Pokémon created or restored by the dungeon
Pokémon system. `DungeonMobManager` separately tracks only vanilla mobs that
carry managed dungeon-mob state.

It does not:

- Scan all entities in every dimension.
- Scan all Pokémon or vanilla mobs near every player.
- Force chunks to remain loaded.
- Reapply effects every game tick.
- Register one tick listener per Pokémon or vanilla mob.

Each managed Pokémon or vanilla-mob record is assigned a deterministic update
bucket from its UUID. This spreads encounter work across ticks while preserving
the configured update interval.

Target searches are:

- Bounded by `detection_range`.
- Performed only for loaded managed encounter entities.
- Reused while the current target remains valid.
- Split between the level player list and bounded living-entity queries only
  when a manager needs non-player faction or legacy targets.

Pokémon persistence is event-driven through Cobblemon save/load events. Vanilla
dungeon mobs persist their encounter state in the entity's normal NBT through a
shared `Mob` mixin, then re-register when that NBT is read. Neither manager
forces chunks to stay loaded.

## Adding another scheduled system

1. Keep the gameplay implementation in its own package.
2. Expose a method such as `tick(MinecraftServer, long)`.
3. Add one explicit call in `ServerTickScheduler`.
4. Keep expensive work behind an interval.
5. Prefer UUID/index lookups over repeated world scans.
6. Clear static runtime state during `SERVER_STOPPED`.
